package it.unibs.ctf.server;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.unibs.ctf.common.Globals;
import it.unibs.ctf.common.InputData;

public class ServerController {
	private final ServerGameModel state;
	private final GameStateParser stateParser;
	private final Deque<Runnable> pendingActions = new ConcurrentLinkedDeque<>();
	private ScheduledExecutorService exec;

	public ServerController(ServerGameModel state, GameStateParser stateParser) {
		this.state = state;
		this.stateParser = stateParser;
	}
	
	public void onPlayerJoined(int id) {
		pendingActions.add(() -> state.addPlayer(id));
		pendingActions.add(() -> stateParser.sendWorldToPlayer(id, state));
	}

	public void onPlayerLeft(int id) {
		pendingActions.add(() -> state.removePlayer(id));
	}

	public void onPlayerInput(int id, InputData input) {
		pendingActions.add(() -> state.applyInput(id, input));
	}
	
	public void start() {
		exec = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "Model-Tick");
			t.setDaemon(true);
			return t;
		});
		exec.scheduleAtFixedRate(this::tick, 0, 1000 / Globals.TPS, TimeUnit.MILLISECONDS );
	}
	
	public void stop() {
		if (exec != null) exec.shutdown();
	}
		
	private void tick() {
		try {
			Runnable action;
			while ((action = pendingActions.poll()) != null) action.run();
			state.update(1f / Globals.TPS);
			stateParser.broadcastPlayers(state);			
		} catch (Throwable t) { // Idk if it can happen, better be safe than sorry
			t.printStackTrace();
		}
	}

}
