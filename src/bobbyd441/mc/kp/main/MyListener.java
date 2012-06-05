package bobbyd441.mc.kp.main;

import java.text.DecimalFormat;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MyListener {
	
	public static Economy econ = null;
	
	public KillsPay kp;
	
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
	
	public MyListener(KillsPay kp)
	{
		this.kp = kp;
		
		killPlayer = kp.getConfig().getBoolean("KillsPay.killplayer.killplayer");
		showPlayerKillMessage = kp.getConfig().getBoolean("KillsPay.killplayer.showpaymessage");
		playerPays = kp.getConfig().getBoolean("KillsPay.killplayer.playerpays");
		playerKillAmount = kp.getConfig().getDouble("KillsPay.killplayer.amount");
		killMob = kp.getConfig().getBoolean("KillsPay.killmob.killmob");
		killFrienlyMob = kp.getConfig().getBoolean("KillsPay.killmob.killfriendlymob");
		showMobKillMessage = kp.getConfig().getBoolean("KillsPay.killmob.showpaymessage");
		mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.defaultmobprice");
		friendlyMobAmount = kp.getConfig().getDouble("KillsPay.killmob.friendlyamount");
		ppermob = kp.getConfig().getBoolean("KillsPay.angrymobprice.ppermob");
		logToConsole = kp.getConfig().getBoolean("KillsPay.messages.logtoconsole");
		percentage = kp.getConfig().getDouble("KillsPay.killplayer.percentage");
		killedPays  = kp.getConfig().getBoolean("KillsPay.killplayer.playerpays");
		
		setupEconomy();
	}
	
	/*
	 * Vault methods:
	 */
	private boolean setupEconomy()
	{
        if (kp.getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = kp.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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
				if(percentage > 0)
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
					pKiller.sendMessage(sf.getFormatedTextMob(kp.getConfig().getString("KillsPay.messages.killedaplayermessage"), (df.format(r.amount) + " " + getCurrency()), pKiller.getName(), player.getName()));
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
						pKiller.sendMessage(sf.getFormatedTextMob(kp.getConfig().getString("KillsPay.messages.playergotkilledmessage"), (df.format(r2.amount) + " " + getCurrency()), pKiller.getName(), player.getName()));
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
			mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.defaultmobprice");
			
			if (ent.getType().toString().equalsIgnoreCase("ZOMBIE"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.zombie");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SPIDER"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.spider");
			}
			else if(ent.getType().toString().equalsIgnoreCase("CREEPER"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.creeper");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SKELETON"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.skeleton");
			}
			else if(ent.getType().toString().equalsIgnoreCase("GIANT"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.giant");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SLIME"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.slime");
			}
			else if(ent.getType().toString().equalsIgnoreCase("GHAST"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.ghast");
			}
			else if(ent.getType().toString().equalsIgnoreCase("PIG_ZOMBIE"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.pigzombie");
			}
			else if(ent.getType().toString().equalsIgnoreCase("ENDERMAN"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.enderman");
			}
			else if(ent.getType().toString().equalsIgnoreCase("CAVE_SPIDER"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.cavespider");
			}
			else if(ent.getType().toString().equalsIgnoreCase("BLAZE"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.blaze");
			}
			else if(ent.getType().toString().equalsIgnoreCase("SILVERFISH"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.silverfish");
			}
			else if(ent.getType().toString().equalsIgnoreCase("MAGMA_CUBE"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.magmacube");
			}
			else if(ent.getType().toString().equalsIgnoreCase("ENDER_DRAGON"))
			{
				angryMob = true;
				if(ppermob)
					mobKillAmount = kp.getConfig().getDouble("KillsPay.angrymobprice.enderdragon");
			}
				
				
			if(killMob && angryMob)
			{
				EconomyResponse r = econ.depositPlayer(killer.getName(), mobKillAmount);
				if (showMobKillMessage)
				{
					DecimalFormat df = new DecimalFormat("0.##");

					StringFormater sf = new StringFormater();
					killer.sendMessage(sf.getFormatedTextMob(kp.getConfig().getString("KillsPay.messages.killedamobmessage"), (df.format(r.amount) + " " + getCurrency()), killer.getName(), evt.getEntityType().getName()));
				}
			}
			if (killFrienlyMob && (!angryMob))
			{
				EconomyResponse r = econ.depositPlayer(killer.getName(), friendlyMobAmount);
				if (showMobKillMessage)
				{
					DecimalFormat df = new DecimalFormat("0.##");
					StringFormater sf = new StringFormater();
					killer.sendMessage(sf.getFormatedTextMob(kp.getConfig().getString("KillsPay.messages.killedamobmessage"), (df.format(r.amount) + " " + getCurrency()), killer.getName(), evt.getEntityType().getName()));
				}	
			}
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
