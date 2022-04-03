package dte.employme.board.listenable;

import dte.employme.board.JobBoard;
import dte.employme.job.Job;

@FunctionalInterface
public interface JobAddListener
{
	void onJobAdded(JobBoard jobBoard, Job job);
}