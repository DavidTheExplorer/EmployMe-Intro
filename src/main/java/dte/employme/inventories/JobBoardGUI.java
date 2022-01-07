package dte.employme.inventories;

import static com.github.stefvanschie.inventoryframework.pane.Orientable.Orientation.HORIZONTAL;
import static dte.employme.utils.ChatColorUtils.bold;
import static dte.employme.utils.ChatColorUtils.createSeparationLine;
import static dte.employme.utils.InventoryUtils.createWall;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.WHITE;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane.Priority;
import com.github.stefvanschie.inventoryframework.pane.PatternPane;
import com.github.stefvanschie.inventoryframework.pane.util.Pattern;

import dte.employme.board.JobBoard;
import dte.employme.items.JobBasicIcon;
import dte.employme.job.Job;
import dte.employme.job.rewards.ItemsReward;
import dte.employme.job.service.JobService;
import dte.employme.utils.items.ItemBuilder;

public class JobBoardGUI extends ChestGui
{
	private final Player player;
	private final JobBoard jobBoard;
	private final Comparator<Job> orderComparator;
	private final JobService jobService;

	private static final Pattern BACKGROUND_PATTERN = new Pattern
			(
					"BBBBBBBBB",
					"B       B",
					"B       B", 
					"B       B", 
					"B       B", 
					"BBBBBBBBB"
					);

	public JobBoardGUI(Player player, JobBoard jobBoard, Comparator<Job> orderComparator, JobService jobService)
	{
		super(6, "Available Jobs");

		this.player = player;
		this.jobBoard = jobBoard;
		this.orderComparator = orderComparator;
		this.jobService = jobService;

		setOnTopClick(event -> event.setCancelled(true));
		addPane(createBackground(Priority.LOWEST));
		addPane(createJobsPane(Priority.LOW));
		update();
	}

	private OutlinePane createJobsPane(Priority priority) 
	{
		OutlinePane pane = new OutlinePane(1, 1, 7, 5, priority);
		pane.setOrientation(HORIZONTAL);

		this.jobBoard.getOfferedJobs().stream()
		.sorted(this.orderComparator)
		.map(this::createOfferIcon)
		.forEach(pane::addItem);

		return pane;
	}

	private GuiItem createOfferIcon(Job job) 
	{
		ItemStack basicIcon = JobBasicIcon.of(job);
		boolean finished = this.jobService.hasFinished(this.player, job);

		//add the status and ID to the lore
		String separator = createSeparationLine(finished ? WHITE : DARK_RED, finished ? 25 : 29);
		String finishMessage = finished ? (bold(GREEN) +  "Click to Finish!") : (RED + "You didn't complete this Job.");

		List<String> lore = basicIcon.getItemMeta().getLore();
		lore.add(separator);
		lore.add(StringUtils.repeat(" ", finished ? 8 : 4) + finishMessage);
		lore.add(separator);

		ItemStack item = new ItemBuilder(basicIcon)
				.withLore(lore.toArray(new String[0]))
				.createCopy();

		return new GuiItem(item, event -> 
		{
			//Right click = preview mode for jobs that offer items
			if(event.isRightClick() && job.getReward() instanceof ItemsReward)
			{
				ItemsRewardPreviewGUI gui = new ItemsRewardPreviewGUI((ItemsReward) job.getReward());
				gui.setOnClose(closeEvent -> this.player.openInventory(event.getInventory()));
				gui.show(this.player);
			}

			//the user wants to finish the job
			else if(this.jobService.hasFinished(this.player, job))
			{
				this.player.closeInventory();
				this.jobBoard.completeJob(job, this.player);
			}
		});
	}

	private static PatternPane createBackground(Priority priority) 
	{
		PatternPane background = new PatternPane(0, 0, 9, 6, BACKGROUND_PATTERN);
		background.bindItem('B', new GuiItem(createWall(Material.BLACK_STAINED_GLASS_PANE)));

		return background;
	}
}
