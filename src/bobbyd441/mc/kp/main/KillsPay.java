package bobbyd441.mc.kp.main;

import java.io.File;
import java.security.Permission;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class KillsPay extends JavaPlugin implements Listener {
	//Get the output console
	private static final Logger log = Logger.getLogger("Minecraft");
	
	//Set variables for Vault
	public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    
    //Set variables for the config file
    public File file;
    public FileConfiguration fileConfig;
    
    public boolean killPlayer;
    public boolean killMob;
    public boolean killFrienlyMob;
    public boolean killedPays;
    public boolean showPlayerKillMessage;
    public boolean showMobKillMessage;
    public boolean playerPays;
    public boolean ppermob;
    public boolean logToConsole;
    public double playerKillAmount;
    public double mobKillAmount;
    public double friendlyMobAmount;
    public double percentage;
    public boolean angryMob;

	//Do stuff on enabling the plugin
	@Override
	public void onEnable()
	{
		//Check if there is an Economy plugin
		if (!setupEconomy() )
		{
			if (logToConsole)
            log.info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            	getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
		//Make sure events can be handled by this plugin
		getServer().getPluginManager().registerEvents(this, this);
		
		//See if there is a config file
		checkConfig();
		
		//Set variables from config file to local variables
		killPlayer = getConfig().getBoolean("KillsPay.killplayer.killplayer");
		showPlayerKillMessage = getConfig().getBoolean("KillsPay.killplayer.showpaymessage");
		playerPays = getConfig().getBoolean("KillsPay.killplayer.playerpays");
		playerKillAmount = getConfig().getDouble("KillsPay.killplayer.amount");
		killMob = getConfig().getBoolean("KillsPay.killmob.killmob");
		killFrienlyMob = getConfig().getBoolean("KillsPay.killmob.killfriendlymob");
		showMobKillMessage = getConfig().getBoolean("KillsPay.killmob.showpaymessage");
		mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.defaultmobprice");
		friendlyMobAmount = getConfig().getDouble("KillsPay.killmob.friendlyamount");
		ppermob = getConfig().getBoolean("KillsPay.angrymobprice.ppermob");
		logToConsole = getConfig().getBoolean("KillsPay.messages.logtoconsole");
		percentage = getConfig().getDouble("KillsPay.killplayer.amount");
		killedPays  = getConfig().getBoolean("KillsPay.killplayer.playerpays");
		
		angryMob = false;
		
		
		//Write to console how the plugin is setup
		if (killPlayer && logToConsole)
		{
				log.info("[KillsPay] Killing players is rewarded");
			if (playerPays)
			{
				log.info("[KillsPay] Killed player pays his killer");
			}
		}
		if (killMob && logToConsole)
		{
			log.info("[KillsPay] Killing mobs is rewarded");
		}
		if (logToConsole)
			log.info("[KillsPay] loaded");
	}
	
	@Override
	public void onDisable()
	{
		//nothing to do here
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return false;
		
	}
	
	//When a player dies
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		//A player died
		Player player = event.getEntity();
		LivingEntity killer = player.getKiller();
		
		//Check if the killer is a player and if killers should be paid
		if (killer instanceof Player && killPlayer)
		{
			//Player got killed by another player, pay the man!
			Player pKiller = (Player)killer;
			
			if (player.getName() != pKiller.getName() && pKiller.hasPermission("killspay.use"))
			{
				if(killedPays)
				{
					double balance = econ.getBalance(player.getName());
					playerKillAmount = (balance/100)*percentage;
				}
				EconomyResponse r = econ.depositPlayer(pKiller.getName(), playerKillAmount);
				
				//Show failures:
				if(!r.transactionSuccess())
		        {
		        	pKiller.sendMessage(String.format(ChatColor.RED + "An error occured: %s", r.errorMessage));
		        }
				//Show message:
				if (showPlayerKillMessage)
				{
					DecimalFormat df = new DecimalFormat("0.##");
					StringFormater sf = new StringFormater();
					pKiller.sendMessage(sf.getFormatedTextMob(getConfig().getString("KillsPay.messages.killedaplayermessage"), (df.format(r.amount) + " " + getCurrency()), pKiller.getName(), player.getName()));
				}
				//The killed player pay his killer
				if (playerPays)
				{
					EconomyResponse r2 = econ.withdrawPlayer(player.getName(), playerKillAmount);
					
					if(!r2.transactionSuccess())
		            {
		            	player.sendMessage(String.format(ChatColor.RED + "An error occured: %s", r2.errorMessage));
		            }
					if (showPlayerKillMessage)
					{
						DecimalFormat df = new DecimalFormat("0.##");
						StringFormater sf = new StringFormater();
						pKiller.sendMessage(sf.getFormatedTextMob(getConfig().getString("KillsPay.messages.playergotkilledmessage"), (df.format(r2.amount) + " " + getCurrency()), pKiller.getName(), player.getName()));
					}
				}
			}
		}
	}
	
	//A living Entity (non player) has died
	@EventHandler
	public void onEntityDeath(EntityDeathEvent evt)
	{
		angryMob = false;
		
		LivingEntity ent = evt.getEntity();
		Player killer = ent.getKiller();
		//Check if the killer is a player
		if (killer instanceof Player && (killMob || killFrienlyMob) && killer.hasPermission("killspay.use"))
		{
			mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.defaultmobprice");
			
			if (ent.getType().toString().equalsIgnoreCase("ZOMBIE"))
			{	
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.zombie");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SPIDER"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.spider");
			}
			else if(ent.getType().toString().equalsIgnoreCase("CREEPER"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.creeper");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SKELETON"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.skeleton");
			}
			else if(ent.getType().toString().equalsIgnoreCase("GIANT"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.giant");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SLIME"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.slime");
			}
			else if(ent.getType().toString().equalsIgnoreCase("GHAST"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.ghast");
			}
			else if(ent.getType().toString().equalsIgnoreCase("PIG_ZOMBIE"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.pigzombie");
			}
			else if(ent.getType().toString().equalsIgnoreCase("ENDERMAN"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.enderman");
			}
			else if(ent.getType().toString().equalsIgnoreCase("CAVE_SPIDER"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.cavespider");
			}
			else if(ent.getType().toString().equalsIgnoreCase("BLAZE"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.blaze");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SILVERFISH"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.silverfish");
			}
			else if(ent.getType().toString().equalsIgnoreCase("MAGMA_CUBE"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.magmacube");
			}
			else if(ent.getType().toString().equalsIgnoreCase("ENDER_DRAGON"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = getConfig().getDouble("KillsPay.angrymobprice.enderdragon");
			}
				
				
			if(killMob && angryMob)
			{
				EconomyResponse r = econ.depositPlayer(killer.getName(), mobKillAmount);
				if (showMobKillMessage)
				{
					DecimalFormat df = new DecimalFormat("0.##");

					StringFormater sf = new StringFormater();
					killer.sendMessage(sf.getFormatedTextMob(getConfig().getString("KillsPay.messages.killedamobmessage"), (df.format(r.amount) + " " + getCurrency()), killer.getName(), evt.getEntityType().getName()));
				}
			}
			if (killFrienlyMob && (!angryMob))
			{
				EconomyResponse r = econ.depositPlayer(killer.getName(), friendlyMobAmount);
				if (showMobKillMessage)
				{
					DecimalFormat df = new DecimalFormat("0.##");
					StringFormater sf = new StringFormater();
					killer.sendMessage(sf.getFormatedTextMob(getConfig().getString("KillsPay.messages.killedamobmessage"), (df.format(r.amount) + " " + getCurrency()), killer.getName(), evt.getEntityType().getName()));
				}	
			}
		}
	}
		
	
	/*
	 * Vault methods:
	 */
	private boolean setupEconomy()
	{
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	private boolean setupChat()
	{
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	/*
	 * Config File
	 */
    public void checkConfig()
    {
	    file = new File(getDataFolder(), "config.yml");
		fileConfig = new YamlConfiguration();
		 if(!file.exists())
		 {                      		 	 
			 // checks if the yaml does not exists
			 if (logToConsole)
				 log.info("[KillsPay] Configuration doesn't exist, creating one");
			 // creates the /plugins/<pluginName>/ directory if not found
			 file.getParentFile().mkdirs();
			try
			{
				 saveDefaultConfig();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	     }
		 else
		 {
			 if (logToConsole)
				 log.info("[KillsPay] Configuration loaded");
		 }
    }
    
    public String getCurrency()
    {
		if ((mobKillAmount > 1) || (mobKillAmount < 0))
			return econ.currencyNamePlural();
		else
			return econ.currencyNameSingular();
    }
 }