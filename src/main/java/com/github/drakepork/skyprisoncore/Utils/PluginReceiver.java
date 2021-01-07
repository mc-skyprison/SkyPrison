package com.github.drakepork.skyprisoncore.Utils;

import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class PluginReceiver extends AbstractModule {

	protected final Core plugin;

	public PluginReceiver(Core plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		this.bind(Core.class).toInstance(this.plugin);
	}
}
