package bobbyd441.mc.kp.main;

import java.io.File;
import java.security.Permission;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class KillsPay extends JavaPlugin implements Listener {
	//Get the output console
	private static final Logger log = Logger.getLogger("Minecraft");
	
	//Set variables for Vault
	public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    
    public boolean logToConsole;
    
    //Set variables for the config file
    public File file;
    
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
		getServer().getPluginManager().registerEvents(new MyListener(this), this);
		
		//See if there is a config file
		checkConfig();
		
		logToConsole = getConfig().getBoolean("KillsPay.messages.logtoconsole");
		
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
	
	/*
	 * Config File
	 */
    public void checkConfig()
    {
	    file = new File(getDataFolder(), "config.yml");
	    
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
 }