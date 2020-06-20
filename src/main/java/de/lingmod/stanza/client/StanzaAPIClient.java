package de.lingmod.stanza.client;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import de.lingmod.stanza.data.AnalysisRequest;
import de.lingmod.stanza.data.TokenOrWord;

public class StanzaAPIClient {

	private Client client;
	private WebTarget target;
	
	public StanzaAPIClient(String url) {
		client = ClientBuilder.newClient();
		target = client.target(url);
	}
	
	@SuppressWarnings("unchecked")
	public List<List<TokenOrWord>> analyze(String text, String lang) {
		AnalysisRequest req = new AnalysisRequest();
		req.setLang(lang);
		req.setText(text);
		return target.request(MediaType.APPLICATION_JSON)
		.post(Entity.entity(req, MediaType.APPLICATION_JSON), List.class);
	}
	
	public static void main(String[] args) {
		StanzaAPIClient client = new StanzaAPIClient("http://localhost:8000/analyze");
		List<List<TokenOrWord>> result = client.analyze("Im Leben läuft manches anders, als man denkt.", "de");
		System.out.println(result);
	}
}
