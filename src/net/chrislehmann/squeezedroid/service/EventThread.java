package net.chrislehmann.squeezedroid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.chrislehmann.squeezedroid.eventhandler.EventHandler;
import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.service.SqueezeService.Event;
import net.chrislehmann.util.SerializationUtils;

public class EventThread extends Thread {
	private Socket _eventSocket;
	private Writer _eventWriter;
	private BufferedReader _eventReader;
	private List<Subscription> _handlers = new ArrayList<Subscription>();
	
	private String host = "localhost";
	private int cliPort = 9090;

	private Pattern eventPattern = Pattern
			.compile("([^ ]*) playlist ([^ ]*) (.*)");

	public EventThread(String host, int cliPort) {
		super();
		this.host = host;
		this.cliPort = cliPort;
	}

	@Override
	public void run() {
		connect();
		try {
			while (!isInterrupted() && _eventSocket.isConnected()) {
				String line = _eventReader.readLine();
				if (line != null) {
					Matcher matcher = eventPattern.matcher(line);
					if (matcher.find()) {
						String playerId = SerializationUtils.decode(matcher
								.group(1));
						String eventType = SerializationUtils.decode(matcher
								.group(2));
						String data = matcher.group(3);
						notify(eventType, playerId, data);
					}
				}

			}
			if (_eventSocket.isClosed()) {
				_eventSocket.close();
			}

		} catch (IOException e) {
			// error reading, just end thread
		}

		_eventSocket = null;
		_eventReader = null;
		_eventWriter = null;

		notify(Event.DISCONNECT.toString(), null, null);

	}

	private void notify(String event, String playerId, String data) {
		synchronized (_handlers) {

			for (Subscription subscription: _handlers) {
				if (subscription.event.toString().toLowerCase().equals(event.toLowerCase()) && subscription.playerId.equals(playerId)) {
					subscription.handler.onEvent(data);
				}
			}
		}
	}

	private void connect() {
		try {
			_eventSocket = new Socket(host, cliPort);
			_eventWriter = new OutputStreamWriter(_eventSocket
					.getOutputStream());
			_eventReader = new BufferedReader(new InputStreamReader(
					_eventSocket.getInputStream()));

			_eventWriter.write("listen 1\n");
			_eventWriter.flush();
			_eventWriter.write("subscribe playlist\n");
			_eventWriter.flush();
		} catch (Exception e) {
			throw new ApplicationException("Cannot connect to squeezeserver", e);
		}
	}

	public synchronized void subscribe(Event event, String playerId, EventHandler handler) {
		synchronized (_handlers) {
			Subscription subscription = new Subscription(event, playerId, handler);
			_handlers.add(subscription);
		}
	}

	public void unsubscribe(Event event, String playerId, EventHandler handler) {
		synchronized (_handlers) {
			Subscription subscription = new Subscription(event, playerId, handler);
			_handlers.remove(subscription);
		}
	}

	private class Subscription {

		public EventHandler handler;
		Event event;
		String playerId;

		public Subscription(Event event, String playerId, EventHandler handler) {
			super();
			this.event = event;
			this.playerId = playerId;
			this.handler = handler;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((event == null) ? 0 : event.hashCode());
			result = prime * result
					+ ((playerId == null) ? 0 : playerId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Subscription other = (Subscription) obj;
			if (event == null) {
				if (other.event != null)
					return false;
			} else if (!event.equals(other.event))
				return false;
			if (playerId == null) {
				if (other.playerId != null)
					return false;
			} else if (!playerId.equals(other.playerId))
				return false;
			return true;
		}
	}

	public void unsubscribeAll(Event event) {
		synchronized (_handlers) {
			
		List<Subscription> subscriptionsToRemove = new ArrayList<Subscription>();
			for (Subscription subscription : _handlers) {
				if( subscription.event.equals(event))
				{
					subscriptionsToRemove.add(subscription);
				}
			}
			
			for (Subscription subscription : subscriptionsToRemove) {
				_handlers.remove(subscription);
			}
		}
	}

}