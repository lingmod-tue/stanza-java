package de.lingmod.stanza.data;

import javax.json.bind.annotation.JsonbTransient;

public class TokenOrWord {

	private String id;
	private String text;
	private String ner;
	private String misc;
	private String lemma;
	private String upos;
	private String xpos;
	private int head;
	private String deprel;
	private String feats;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getNer() {
		return ner;
	}
	public void setNer(String ner) {
		this.ner = ner;
	}
	public String getMisc() {
		return misc;
	}
	public void setMisc(String misc) {
		this.misc = misc;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getUpos() {
		return upos;
	}
	public void setUpos(String upos) {
		this.upos = upos;
	}
	public String getXpos() {
		return xpos;
	}
	public void setXpos(String xpos) {
		this.xpos = xpos;
	}
	public int getHead() {
		return head;
	}
	public void setHead(int head) {
		this.head = head;
	}
	public String getDeprel() {
		return deprel;
	}
	public void setDeprel(String deprel) {
		this.deprel = deprel;
	}
	public String getFeats() {
		return feats;
	}
	public void setFeats(String feats) {
		this.feats = feats;
	}
	
	@JsonbTransient
	public boolean isWord() {
		return lemma != null;
	}
	
	@JsonbTransient
	public boolean isToken() {
		return misc.contains("start_char");
	}
	@Override
	public String toString() {
		return "TokenOrWord [id=" + id + ", text=" + text + ", ner=" + ner + ", misc=" + misc + ", lemma=" + lemma
				+ ", upos=" + upos + ", xpos=" + xpos + ", head=" + head + ", deprel=" + deprel + ", feats=" + feats
				+ "]";
	}
	
	
}
