package dte.employme.conversations;

import static dte.employme.messages.MessageKey.ITEM_GOAL_BLOCKED_IN_YOUR_WORLD;
import static dte.employme.messages.MessageKey.ITEM_GOAL_INVALID;
import static dte.employme.utils.java.Predicates.negate;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;

import dte.employme.services.job.JobService;
import dte.employme.services.message.MessageService;
import dte.employme.utils.MaterialUtils;

public class JobGoalPrompt extends ValidatingPrompt
{
	private final JobService jobService;
	private final MessageService messageService;
	private final String question;
	
	private boolean blockedItemSpecified = false;
	
	public JobGoalPrompt(JobService jobService, MessageService messageService, String question) 
	{
		this.jobService = jobService;
		this.messageService = messageService;
		this.question = question;
	}

	@Override
	public String getPromptText(ConversationContext context)
	{
		return this.question;
	}

	@Override
	protected boolean isInputValid(ConversationContext context, String input)
	{
		World world = ((Player) context.getForWhom()).getWorld();
		Material material = Material.matchMaterial(input);
		
		this.blockedItemSpecified = this.jobService.isBlacklistedAt(world, material);
		
		return Optional.ofNullable(material)
				.filter(MaterialUtils::isObtainable)
				.filter(negate(self -> this.blockedItemSpecified))
				.isPresent();
	}
	
	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) 
	{
		context.setSessionData("material", Material.matchMaterial(input));
		
		return Prompt.END_OF_CONVERSATION;
	}
	
	@Override
	protected String getFailedValidationText(ConversationContext context, String invalidInput) 
	{
		if(this.blockedItemSpecified) 
		{
			this.blockedItemSpecified = false;
			return this.messageService.getMessage(ITEM_GOAL_BLOCKED_IN_YOUR_WORLD).first();
		}
		
		return this.messageService.getMessage(ITEM_GOAL_INVALID).first();
	}
}