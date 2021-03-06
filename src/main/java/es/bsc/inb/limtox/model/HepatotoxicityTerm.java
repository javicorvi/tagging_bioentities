package es.bsc.inb.limtox.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class HepatotoxicityTerm implements LimtoxEntity {
	
	private Integer keyId;
	
	private String etox_mapping_id;
	
	private String mesh_omim_mapping_id;
	
	private String mouse_pathology_mapping_id;
	
    private String MedDRA_mapping_id;
    
    private String gemina_sympton_mapping_id;
    
    private String disease_ontology_mapping_id;
    
    private String speech_entry;
    
    private String polysearch_mapping_id;
    
    private String adverse_events_mapping_id;
    
    private String stemmed_entry;
    
    private String original_entry;
    
    private String human_phenotype_mapping_id;
    
	private String EFPIA_mapping_id;
	
	private String mapped;
	
	private String MPheno_mapping_id;
	
	private String COSTART_concept;
	
	public void toLowerCase() {
		if(original_entry!=null && !original_entry.equals(original_entry.toLowerCase())) {
			original_entry=original_entry.toLowerCase();
		}
	}
	
	
	
	public Integer getKeyId() {
		return keyId;
	}



	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}



	public String getEtox_mapping_id() {
		return etox_mapping_id;
	}

	public void setEtox_mapping_id(String etox_mapping_id) {
		this.etox_mapping_id = etox_mapping_id;
	}

	public String getMesh_omim_mapping_id() {
		return mesh_omim_mapping_id;
	}

	public void setMesh_omim_mapping_id(String mesh_omim_mapping_id) {
		this.mesh_omim_mapping_id = mesh_omim_mapping_id;
	}

	public String getMouse_pathology_mapping_id() {
		return mouse_pathology_mapping_id;
	}

	public void setMouse_pathology_mapping_id(String mouse_pathology_mapping_id) {
		this.mouse_pathology_mapping_id = mouse_pathology_mapping_id;
	}

	public String getMedDRA_mapping_id() {
		return MedDRA_mapping_id;
	}

	public void setMedDRA_mapping_id(String medDRA_mapping_id) {
		MedDRA_mapping_id = medDRA_mapping_id;
	}

	public String getGemina_sympton_mapping_id() {
		return gemina_sympton_mapping_id;
	}

	public void setGemina_sympton_mapping_id(String gemina_sympton_mapping_id) {
		this.gemina_sympton_mapping_id = gemina_sympton_mapping_id;
	}

	public String getDisease_ontology_mapping_id() {
		return disease_ontology_mapping_id;
	}

	public void setDisease_ontology_mapping_id(String disease_ontology_mapping_id) {
		this.disease_ontology_mapping_id = disease_ontology_mapping_id;
	}

	public String getSpeech_entry() {
		return speech_entry;
	}

	public void setSpeech_entry(String speech_entry) {
		this.speech_entry = speech_entry;
	}

	public String getPolysearch_mapping_id() {
		return polysearch_mapping_id;
	}

	public void setPolysearch_mapping_id(String polysearch_mapping_id) {
		this.polysearch_mapping_id = polysearch_mapping_id;
	}

	public String getAdverse_events_mapping_id() {
		return adverse_events_mapping_id;
	}

	public void setAdverse_events_mapping_id(String adverse_events_mapping_id) {
		this.adverse_events_mapping_id = adverse_events_mapping_id;
	}

	public String getStemmed_entry() {
		return stemmed_entry;
	}

	public void setStemmed_entry(String stemmed_entry) {
		this.stemmed_entry = stemmed_entry;
	}

	public String getOriginal_entry() {
		return original_entry;
	}

	public void setOriginal_entry(String original_entry) {
		this.original_entry = original_entry;
	}

	public String getHuman_phenotype_mapping_id() {
		return human_phenotype_mapping_id;
	}

	public void setHuman_phenotype_mapping_id(String human_phenotype_mapping_id) {
		this.human_phenotype_mapping_id = human_phenotype_mapping_id;
	}

	public String getEFPIA_mapping_id() {
		return EFPIA_mapping_id;
	}

	public void setEFPIA_mapping_id(String eFPIA_mapping_id) {
		EFPIA_mapping_id = eFPIA_mapping_id;
	}

	public String getMapped() {
		return mapped;
	}

	public void setMapped(String mapped) {
		this.mapped = mapped;
	}

	public String getMPheno_mapping_id() {
		return MPheno_mapping_id;
	}

	public void setMPheno_mapping_id(String mPheno_mapping_id) {
		MPheno_mapping_id = mPheno_mapping_id;
	}

	public String getCOSTART_concept() {
		return COSTART_concept;
	}

	public void setCOSTART_concept(String cOSTART_concept) {
		COSTART_concept = cOSTART_concept;
	}

	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }else if(original_entry==null || ((HepatotoxicityTerm)obj).original_entry==null) {
	    	return false;
	    }else {
	    	if(original_entry.equals(((HepatotoxicityTerm)obj).original_entry)) {
	    		return true;
	    	}
	    	return false;
	    }
	    
	}

	@Override
	public int hashCode() {
	    return original_entry.hashCode();
	}
	
	
}
