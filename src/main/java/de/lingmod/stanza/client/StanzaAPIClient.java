package de.lingmod.stanza.client;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.lingmod.stanza.data.AnalysisRequest;
import de.lingmod.stanza.data.TokenOrWord;
import de.lingmod.stanza.util.AnnotationUtils;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class StanzaAPIClient {

	private Client client;
	private WebTarget target;
	
	public StanzaAPIClient(String url) {
		client = ClientBuilder.newClient();
		target = client.target(url);
	}
	
	public List<List<TokenOrWord>> analyze(String text, String lang) {
		AnalysisRequest req = new AnalysisRequest();
		req.setLang(lang);
		req.setText(text);
		return target.request(MediaType.APPLICATION_JSON)
		.post(Entity.entity(req, MediaType.APPLICATION_JSON), new GenericType<List<List<TokenOrWord>>>() {});
	}
	
	public static void main(String[] args) throws ResourceInitializationException, CASException, IOException {
		StanzaAPIClient client = new StanzaAPIClient("http://rinia.sfs.uni-tuebingen.de:8000/analyze");
		String inputText = "Im Leben läuft manches anders, als man denkt. Hier ist noch ein Satz.";
		String langCode = "de";
		List<List<TokenOrWord>> result = client.analyze(inputText, langCode);
		System.out.println(result);
		
		JCas jcas = JCasFactory.createJCas();
		AnnotationUtils.annotateDkPro(inputText, langCode, result, jcas);
		for (Dependency d : JCasUtil.select(jcas, Dependency.class)) {
			System.out.println(d);
		}
	}
}
