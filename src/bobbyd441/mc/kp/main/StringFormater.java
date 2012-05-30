package bobbyd441.mc.kp.main;

import org.bukkit.ChatColor;

public class StringFormater {
	
	public String killer;
	public String died;
	public String mob;
	public String amount;
	
	public StringBuilder sb;
	public String[] textArray;
	public ChatColor chatColor;
	
	public String getFormatedTextMob(String text, String amount, String killer, String mob)
	{
		textArray = text.split(" ");
		
		this.killer = killer;
		this.mob = mob;
		this.amount = amount;
		
		return buildText();
	}
	
	public String getFormatedTextPlayers(String text, String amount, String killer, String died)
	{
		textArray = text.split(" ");
		
		this.killer = killer;
		this.died = died;
		this.amount = amount;
		
		return buildText();
	}
	
	private String buildText()
	{
		sb = new StringBuilder();
		for(String chunk : textArray)
		{
			if(chunk.contains("%"))
			{
				if(chunk == "%0")
					sb.append(ChatColor.BLACK);
				else if (chunk.equalsIgnoreCase("%1"))
					sb.append(ChatColor.DARK_BLUE);
				else if(chunk.equalsIgnoreCase("%2"))
					sb.append(ChatColor.DARK_GREEN);
				else if(chunk.equalsIgnoreCase("%3"))
					sb.append(ChatColor.WHITE);
				else if(chunk.equalsIgnoreCase("%4"))
					sb.append(ChatColor.DARK_RED);
				else if(chunk.equalsIgnoreCase("%5"))
					sb.append(ChatColor.DARK_PURPLE);
				else if(chunk.equalsIgnoreCase("%6"))
					sb.append(ChatColor.GOLD);
				else if(chunk.equalsIgnoreCase("%7"))
					sb.append(ChatColor.GRAY);
				else if(chunk.equalsIgnoreCase("%8"))
					sb.append(ChatColor.DARK_GRAY);
				else if(chunk.equalsIgnoreCase("%9"))
					sb.append(ChatColor.BLUE);
				else if(chunk.equalsIgnoreCase("%a"))
					sb.append(ChatColor.GREEN);
				else if(chunk.equalsIgnoreCase("%b"))
					sb.append(ChatColor.AQUA);
				else if(chunk.equalsIgnoreCase("%c"))
					sb.append(ChatColor.RED);
				else if(chunk.equalsIgnoreCase("%d"))
					sb.append(ChatColor.LIGHT_PURPLE);
				else if(chunk.equalsIgnoreCase("%e"))
					sb.append(ChatColor.YELLOW);
				else if(chunk.equalsIgnoreCase("%f"))
					sb.append(ChatColor.WHITE);
				else if(chunk.equalsIgnoreCase("%killer"))
					sb.append(killer + " ");
				else if(chunk.equalsIgnoreCase("%died"))
					sb.append(died + " ");
				else if(chunk.equalsIgnoreCase("%amount"))
					sb.append(amount + " ");
				else if(chunk.equalsIgnoreCase("%mob"))
					sb.append(mob  + " ");
				else
					sb.append(chunk + " ");
			}
			else
				sb.append(chunk + " ");	
		}
		return sb.toString();
	}

}
