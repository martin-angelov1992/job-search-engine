package marto.job_search_engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import static org.elasticsearch.common.xcontent.XContentFactory.*;

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
		try {
			XContentBuilder builder = jsonBuilder().startObject();

			if (company != null) {
				builder.field("company", company);
			}

			if (jobTitle != null) {
				builder.field("jobTitle", jobTitle);
			}

			builder.field("text", text);

			for (Map.Entry<String, String> entry : categoryTypes.entrySet()) {
				String metaKey = entry.getKey();
				String metaValue = entry.getValue();

				builder.field(metaKey, metaValue);
			}

            builder.endObject();
			IndexResponse response = client.prepareIndex("jobs", company)
			        .setSource(builder)
			        .get();
//			if (response.status().getStatus()) {
//				
//			}
		} catch (IOException e) {
			System.err.println("Unable to create document.");
			e.printStackTrace();
		}
	}

	public void processJobWithUnknownFormat(String text, String company, String jobTitle) {
		XContentBuilder builder;
		try {
			builder = jsonBuilder().startObject().field("text", text)
					.field("jobTitle", jobTitle)
					.field("company", company).endObject();

			IndexResponse response = client.prepareIndex("jobs", company)
			        .setSource(builder)
			        .get();
		} catch (IOException e) {
			System.err.println("Unable to create document.");
			e.printStackTrace();
		}
	}

	public void close() {
		client.close();
	}
}
