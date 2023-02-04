package dte.employme.board;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import dte.employme.board.listeners.addition.JobAddListener;
import dte.employme.board.listeners.completion.JobCompleteListener;
import dte.employme.board.listeners.removal.JobRemovalListener;
import dte.employme.job.Job;

public class SimpleJobBoard implements JobBoard
{
	private final Map<UUID, Job> jobByUUID = new LinkedHashMap<>();
	
	//listeners
	private final Set<JobAddListener> addListeners = new LinkedHashSet<>();
	private final Set<JobCompleteListener> completeListeners = new LinkedHashSet<>();
	private final Set<JobRemovalListener> removalListeners = new LinkedHashSet<>();
	
	@Override
	public void addJob(Job job) 
	{
		this.jobByUUID.put(job.getUUID(), job);
		
		this.addListeners.forEach(listener -> listener.onJobAdded(this, job));
	}
	
	@Override
	public void removeJob(Job job) 
	{
		this.jobByUUID.remove(job.getUUID());
		
		this.removalListeners.forEach(listener -> listener.onJobRemoved(this, job));
	}
	
	@Override
	public boolean containsJob(Job job)
	{
		return this.jobByUUID.containsKey(job.getUUID());
	}
	
	@Override
	public void completeJob(Job job, Player whoCompleted, JobCompletionContext context) 
	{
		this.completeListeners.forEach(listener -> listener.onJobCompleted(job, whoCompleted, context));
		
		if(context.isJobCompleted())
			removeJob(job);
	}
	
	@Override
	public Optional<Job> getJobByUUID(UUID uuid) 
	{
		return Optional.ofNullable(this.jobByUUID.get(uuid));
	}
	
	@Override
	public List<Job> getJobsOfferedBy(UUID employerUUID) 
	{
		return this.jobByUUID.values().stream()
				.filter(job -> job.getEmployer().getUniqueId().equals(employerUUID))
				.collect(toList());
	}
	
	@Override
	public List<Job> getOfferedJobs()
	{
		return new ArrayList<>(this.jobByUUID.values());
	}
	
	@Override
	public void registerAddListener(JobAddListener listener) 
	{
		this.addListeners.add(listener);
	}

	@Override
	public void registerCompleteListener(JobCompleteListener listener) 
	{
		this.completeListeners.add(listener);
	}

	@Override
	public void registerRemovalListener(JobRemovalListener listener) 
	{
		this.removalListeners.add(listener);
	}
	
	@Override
	public void removeAddListener(JobAddListener listener) 
	{
		this.addListeners.remove(listener);
	}
	
	@Override
	public void removeCompleteListener(JobCompleteListener listener) 
	{
		this.completeListeners.remove(listener);
	}
	
	@Override
	public void removeRemovalListener(JobRemovalListener listener) 
	{
		this.removalListeners.remove(listener);
	}
	
	@Override
	public Iterator<Job> iterator() 
	{
		return this.jobByUUID.values().iterator();
	}
}