package marto.job_search_engine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class Storage {
	private TransportClient client;

	public Storage() {
		try {
			client = new PreBuiltTransportClient(Settings.EMPTY);
			client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			System.err.println("Unable to create client.");
			e.printStackTrace();
		}
	}

	public void processJobWithKnownFormat(Map<String, String> categoryTypes, String company, String jobTitle, String text) {
		
	}

	public void processJobWithUnknownFormat(String text) {
		
	}

	public void close() {
		client.close();
	}
}
