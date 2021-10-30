package dte.employme.listeners;

import static dte.employme.messages.MessageKey.ITEMS_JOB_NO_ITEMS_WARNING;
import static dte.employme.messages.MessageKey.JOB_SUCCESSFULLY_DELETED;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import dte.employme.board.InventoryJobBoard;
import dte.employme.board.JobBoard;
import dte.employme.conversations.Conversations;
import dte.employme.items.ItemFactory;
import dte.employme.messages.MessageService;
import dte.employme.utils.InventoryUtils;

public class JobInventoriesListener implements Listener
{
	private final Conversations conversations;
	private final ItemFactory itemFactory;
	private final JobBoard globalJobBoard;
	private final MessageService messageService;
	
	public JobInventoriesListener(JobBoard globalJobBoard, ItemFactory itemFactory, Conversations conversations, MessageService messageService) 
	{
		this.globalJobBoard = globalJobBoard;
		this.itemFactory = itemFactory;
		this.conversations = conversations;
		this.messageService = messageService;
	}
	
	@EventHandler
	public void onJobComplete(InventoryClickEvent event) 
	{
		InventoryJobBoard.getRepresentedBoard(event.getInventory()).ifPresent(inventoryBoard -> 
		{
			event.setCancelled(true);
			ItemStack item = event.getCurrentItem();
			
			if(item == null)
				return;
			
			Player player = (Player) event.getWhoClicked();
			
			this.itemFactory.getJobID(item)
			.flatMap(inventoryBoard::getJobByID)
			.filter(job -> job.hasFinished(player))
			.ifPresent(job ->
			{
				player.closeInventory();
				this.globalJobBoard.completeJob(job, player);
			});
		});
	}
	
	@EventHandler
	public void onDeletionMark(InventoryClickEvent event) 
	{
		if(!event.getView().getTitle().equals("Select Jobs to Delete"))
			return;
		
		event.setCancelled(true);
		
		ItemStack item = event.getCurrentItem();
		
		if(item == null)
			return;
		
		Player employer = (Player) event.getWhoClicked();
		
		this.itemFactory.getJobID(item)
		.flatMap(this.globalJobBoard::getJobByID)
		.ifPresent(job -> 
		{
			employer.closeInventory();
			this.globalJobBoard.removeJob(job);
			job.getReward().giveTo(employer);
			
			this.messageService.sendTo(employer, JOB_SUCCESSFULLY_DELETED);
		});
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventHandler
	public void onTypeSelect(InventoryClickEvent event) 
	{
		if(!event.getView().getTitle().equals("Create a new Job"))
			return;
		
		event.setCancelled(true);
		
		ItemStack item = event.getCurrentItem();
		
		if(item == null)
			return;
		
		Player employer = (Player) event.getWhoClicked();
		
		switch(event.getCurrentItem().getType())
		{
		case GOLD_INGOT:
			this.conversations.buildMoneyJobConversation(employer).begin();
			employer.closeInventory();
			break;
			
		case CHEST:
			employer.openInventory(Bukkit.createInventory(null, 9 * 6, "What would you like to offer?"));
			break;
		}
	}
	
	@EventHandler
	public void onItemsJobOfferingInventory(InventoryCloseEvent event) 
	{
		if(!event.getView().getTitle().equals("What would you like to offer?"))
			return;
		
		Player player = (Player) event.getPlayer();
		List<ItemStack> offeredItems = InventoryUtils.itemsStream(event.getInventory(), true).collect(toList());
		
		if(offeredItems.isEmpty()) 
		{
			this.messageService.sendGeneralMessage(player, ITEMS_JOB_NO_ITEMS_WARNING);
			return;
		}
		
		this.conversations.buildItemsJobConversation(player, offeredItems).begin();
	}
	
	@EventHandler
	public void onContainersClick(InventoryClickEvent event) 
	{
		if(!event.getView().getTitle().matches("Claim your [a-zA-Z]+:"))
			return;
		
		switch(event.getRawSlot()) 
		{
		case 43:
		case 44:
		case 52:
		case 53:
			event.setCancelled(true);
		}
	}
}
