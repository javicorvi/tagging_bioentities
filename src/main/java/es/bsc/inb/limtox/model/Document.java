package es.bsc.inb.limtox.model;

import java.util.ArrayList;
import java.util.List;



public class Document implements LimtoxEntity {

	private Integer id;
	
	private String sourceId;
	
	private String fulltext;
	
	private Double score;
	
	private List<Sentence> sentences = new ArrayList<Sentence>();
	
	private List<MeshChemicalCompound> meshChemicalCompounds = new ArrayList<MeshChemicalCompound>();
	
	private String outputPath;
	
	private Double executionTime;
	
	public Document() {
		super();
	}
	public Document(String sourceId, String outputPath) {
		this.sourceId=sourceId;
		this.outputPath=outputPath;
	}
	
	public Document(String sourceId, String fulltext, Double score) {
		this.sourceId=sourceId;
		this.fulltext=fulltext;
		this.score=score;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public List<MeshChemicalCompound> getMeshChemicalCompounds() {
		return meshChemicalCompounds;
	}
	public void setMeshChemicalCompounds(List<MeshChemicalCompound> meshChemicalCompounds) {
		this.meshChemicalCompounds = meshChemicalCompounds;
	}
	public String getOutputPath() {
		return outputPath;
	}
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	public Double getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(Double executionTime) {
		this.executionTime = executionTime;
	}
	public String getFulltext() {
		return fulltext;
	}
	public void setFulltext(String fulltext) {
		this.fulltext = fulltext;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	
	
	
	
	
	
}
