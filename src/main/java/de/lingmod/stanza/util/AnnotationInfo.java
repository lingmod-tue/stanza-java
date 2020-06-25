package de.lingmod.stanza.util;

import de.lingmod.stanza.data.TokenOrWord;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

class AnnotationInfo implements Comparable<AnnotationInfo> {
	String id;
	int begin;
	int end;
	TokenOrWord stanzaToken;
	Token uimaToken;
	Dependency uimaDependentAnn;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + begin;
		result = prime * result + end;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AnnotationInfo other = (AnnotationInfo) obj;
		if (begin != other.begin)
			return false;
		if (end != other.end)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(AnnotationInfo o) {
		return Integer.compare(this.begin, o.begin);
	}
	
	
}