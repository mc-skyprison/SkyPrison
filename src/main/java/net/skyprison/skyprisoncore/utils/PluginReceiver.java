package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class PluginReceiver extends AbstractModule {

	protected final SkyPrisonCore plugin;

	public PluginReceiver(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		this.bind(SkyPrisonCore.class).toInstance(this.plugin);
	}
}
