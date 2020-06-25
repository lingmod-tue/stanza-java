package de.lingmod.stanza.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import de.lingmod.stanza.data.TokenOrWord;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class AnnotationUtils {

	private static final Pattern stanzaOffsetPattern = Pattern.compile("start_char=(\\d+)\\|end_char=(\\d+)");
	
	public static void annotateDkPro(String inputText, String langCode, List<List<TokenOrWord>> stanzaOutput, JCas jcas) {
		
		StringBuilder docText = new StringBuilder(inputText);
		List<AnnotationInfo> surfaceForms = new ArrayList<AnnotationInfo>();
		List<List<AnnotationInfo>> tokens = new ArrayList<List<AnnotationInfo>>();
		
		int offset = 0;
		// collect info on the relationship between words and surface tokens
		for (List<TokenOrWord> sentence : stanzaOutput) {
			List<AnnotationInfo> sentTokens = new ArrayList<AnnotationInfo>();
			tokens.add(sentTokens);
			for (int t = 0; t < sentence.size(); t++) {
				TokenOrWord tow = sentence.get(t);
				if (tow.isToken()) {
					// surface token or token-word
					AnnotationInfo annInfo = getAnnotationInfo(tow);
					annInfo.begin += offset;
					annInfo.end += offset;
					if (tow.isWord()) {
						sentTokens.add(annInfo);
					} else {
						// surface token only, need to keep track
						String[] fromTo = tow.getId().split("-", 2);
						List<String> components = new ArrayList<String>();
						int pos = annInfo.begin;
						for (int i = Integer.parseInt(fromTo[0]); i <= Integer.parseInt(fromTo[1]); i++) {
							TokenOrWord word = findWord(i, sentence, t+1);
							AnnotationInfo wordInfo = new AnnotationInfo();
							wordInfo.id = word.getId();
							wordInfo.begin = pos;
							wordInfo.end = pos + word.getText().length();
							wordInfo.stanzaToken = word;
							sentTokens.add(wordInfo);
							pos = wordInfo.end + 1;
							components.add(word.getText());
						}
						// replace surface tokens by real words in input text and adjust offset
						String original = tow.getText();
						String replacement = String.join(" ", components);
						docText.replace(annInfo.begin, annInfo.end, replacement);
						int diff = replacement.length() - original.length();
						annInfo.end += diff;
						surfaceForms.add(annInfo);
						offset += diff;
					}
				}
			}
		}
		
		// initialize CAS
		jcas.setDocumentText(docText.toString());
		jcas.setDocumentLanguage(langCode);
		
		// annotate surface forms
		for (AnnotationInfo sForm : surfaceForms) {
			SurfaceForm ann = new SurfaceForm(jcas);
			ann.setBegin(sForm.begin);
			ann.setEnd(sForm.end);
			ann.setValue(sForm.stanzaToken.getText());
			ann.addToIndexes(jcas);
			
			if (sForm.stanzaToken.getNer() != null) {
				// NER info, add annotation for that
				NamedEntity ner = new NamedEntity(jcas);
				ner.setBegin(sForm.begin);
				ner.setEnd(sForm.end);
				ner.setValue(sForm.stanzaToken.getNer());
				ner.addToIndexes(jcas);
			}
		}
		
		// annotate tokens
		for (List<AnnotationInfo> sentTokens : tokens) {
			Sentence sentence = new Sentence(jcas);
			sentence.setBegin(sentTokens.get(0).begin);
			sentence.setEnd(sentTokens.get(sentTokens.size()-1).end);
			sentence.addToIndexes(jcas);
			
			for (AnnotationInfo word : sentTokens) {
				// token
				Token token = new Token(jcas);
				token.setBegin(word.begin);
				token.setEnd(word.end);
				token.addToIndexes(jcas);	
				word.uimaToken = token;
				
				// lemma
				Lemma lemma = new Lemma(jcas);
				lemma.setBegin(word.begin);
				lemma.setEnd(word.end);
				lemma.setValue(word.stanzaToken.getLemma());
				lemma.addToIndexes(jcas);
				token.setLemma(lemma);

				// POS
				POS pos = new POS(jcas);
				pos.setBegin(word.begin);
				pos.setEnd(word.end);
				pos.setPosValue(word.stanzaToken.getXpos());
				pos.setCoarseValue(word.stanzaToken.getUpos());
				pos.addToIndexes(jcas);
				token.setPos(pos);

				// morphology
				if (word.stanzaToken.getFeats() != null) {
					String featsString = word.stanzaToken.getFeats();
					MorphologicalFeatures feats = new MorphologicalFeatures(jcas);
					feats.setBegin(word.begin);
					feats.setEnd(word.end);
					feats.setValue(featsString);
					Type type = feats.getType();

					Map<String, String> featMap = featsToMap(featsString);
					for (String feat : featMap.keySet()) {
						String lcFeat = Character.toLowerCase(feat.charAt(0)) + feat.substring(1);
						Feature uimaFeat = type.getFeatureByBaseName(lcFeat);
						if (uimaFeat != null) {
							String val = featMap.get(feat);
							feats.setFeatureValueFromString(uimaFeat, val);
						}
					}
					feats.addToIndexes(jcas);
					token.setMorph(feats);
				}

				// dependencies
				Dependency dep = new Dependency(jcas);
				dep.setBegin(word.begin);
				dep.setEnd(word.end);
				dep.setDependencyType(word.stanzaToken.getDeprel());
				dep.setDependent(token);
				dep.addToIndexes(jcas);
				word.uimaDependentAnn = dep;
			}
			// now that we have all the token annotations, set the dependency heads
			for (AnnotationInfo word : sentTokens) {
				word.uimaDependentAnn.setGovernor(
						sentTokens.get(word.stanzaToken.getHead()).uimaToken);
			}
		}
	}
	
	private static Map<String, String> featsToMap(String udFeatsString) {
		HashMap<String, String> featMap = new HashMap<String, String>();
		for (String pair : udFeatsString.split("\\|")) {
			String[] featVal = pair.split("=");
			featMap.put(featVal[0], featVal[1]);
		}
		return featMap;
	}
	
	private static TokenOrWord findWord(int id, List<TokenOrWord> sequence, int start) {
		for (int i = start; i < sequence.size(); i++) {
			TokenOrWord tow = sequence.get(i);
			if (id == Integer.parseInt(tow.getId())) {
				return tow;
			}
		}
		return null;
	}
	
	public static AnnotationInfo getAnnotationInfo(TokenOrWord tow) {
		if (!tow.isToken()) {
			return null;
		}
		AnnotationInfo annInf = new AnnotationInfo();
		annInf.id = tow.getId();
		Matcher m = stanzaOffsetPattern.matcher(tow.getMisc());
		m.matches();
		annInf.begin = Integer.parseInt(m.group(1));
		annInf.end = Integer.parseInt(m.group(2));
		annInf.stanzaToken = tow;
		return annInf;
	}
	
}
