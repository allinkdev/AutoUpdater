package me.allinkdev.autoupdater.common.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UpdateTask implements Runnable {

	protected final Logger logger = LoggerFactory.getLogger("Update Task");
}
