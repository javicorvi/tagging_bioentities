package es.bsc.inb.limtox.services;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.objectbank.ObjectBank;
import es.bsc.inb.limtox.exceptions.MoreThanOneEntityException;
import es.bsc.inb.limtox.model.ChemicalCompound;
import es.bsc.inb.limtox.model.ChemicalCompoundCytochromeSentence;
import es.bsc.inb.limtox.model.ChemicalCompoundHepatotoxicityTermPattern;
import es.bsc.inb.limtox.model.ChemicalCompoundSentence;
import es.bsc.inb.limtox.model.Cytochrome;
import es.bsc.inb.limtox.model.CytochromeChemicalCompoundInductionPattern;
import es.bsc.inb.limtox.model.CytochromeChemicalCompoundInhibitionPattern;
import es.bsc.inb.limtox.model.CytochromeChemicalCompoundMetabolismPattern;
import es.bsc.inb.limtox.model.CytochromeSentence;
import es.bsc.inb.limtox.model.Document;
import es.bsc.inb.limtox.model.HepatotoxicityTerm;
import es.bsc.inb.limtox.model.HepatotoxicityTermChemicalCompoundSentence;
import es.bsc.inb.limtox.model.HepatotoxicityTermSentence;
import es.bsc.inb.limtox.model.Marker;
import es.bsc.inb.limtox.model.MarkerChemicalCompoundSentence;
import es.bsc.inb.limtox.model.MarkerSentence;
import es.bsc.inb.limtox.model.Ocurrence;
import es.bsc.inb.limtox.model.RelationRule;
import es.bsc.inb.limtox.model.Sentence;
import es.bsc.inb.limtox.model.Taxonomy;
import es.bsc.inb.limtox.model.TaxonomySentence;
import es.bsc.inb.limtox.util.Constants;
@Service
class TaggingServiceImpl implements TaggingService {

	static final Logger taggingLog = Logger.getLogger("taggingLog");
	
	@Autowired
	private DictionaryService dictionaryService;
	
	private HashMap<String, Pattern> patterns = new HashMap<String,Pattern>();
	
	public void execute(String propertiesParametersPath) {
		try {
			dictionaryService.execute();
			taggingLog.info("Tagging articles with properties :  " +  propertiesParametersPath);
			Properties propertiesParameters = this.loadPropertiesParameters(propertiesParametersPath);
			taggingLog.info("Input directory with the articles to tag : " + propertiesParameters.getProperty("inputDirectory"));
			taggingLog.info("Input directory with sentences the articles to tag : " + propertiesParameters.getProperty("inputDirectorySentences"));
			taggingLog.info("Outup directory with the relevant articles : " + propertiesParameters.getProperty("outputDirectory"));
			
			String inputDirectoryPath = propertiesParameters.getProperty("inputDirectory");
			String inputDirectorySentencesPath = propertiesParameters.getProperty("inputDirectorySentences");
			String outputDirectoryPath = propertiesParameters.getProperty("outputDirectory");
			
			File inputDirectory = new File(inputDirectoryPath);
		    if(!inputDirectory.exists()) {
		    	return ;
		    }
		    if (!Files.isDirectory(Paths.get(inputDirectoryPath))) {
		    	return ;
		    }
		    File outputDirectory = new File(outputDirectoryPath);
		    if(!outputDirectory.exists())
		    	outputDirectory.mkdirs();
		    
		    List<String> filesProcessed = readFilesProcessed(outputDirectoryPath); 
		    BufferedWriter filesPrecessedWriter = new BufferedWriter(new FileWriter(outputDirectoryPath + File.separator + "list_files_processed.txt", true));
		    File[] files =  inputDirectory.listFiles();
			for (File file_to_classify : files) {
				
				if(file_to_classify.getName().endsWith(".txt") && !file_to_classify.getName().contains("sentences") && filesProcessed!=null && !filesProcessed.contains(file_to_classify.getName())){
					
					String fileName = file_to_classify.getName();
					String outputFile = outputDirectory + File.separator + fileName + ".json";
					Map<String, Document> documents_map = this.loadDocuments(file_to_classify);
					if (Files.isRegularFile(Paths.get(inputDirectorySentencesPath + File.separator + fileName + "_sentences.txt"))) {
						File inputFileSentences = new File(inputDirectorySentencesPath + File.separator + fileName + "_sentences.txt");
						this.loadSentences(documents_map, inputFileSentences);
				    }
					
					Collection<Document> documents = documents_map.values();
					for (Document document : documents) {
						this.tagging(document);
						generateJSONFileForDocument(document, outputDirectory + File.separator + document.getSourceId() + ".json");
					}
					
					generateJSONFile(documents, outputFile);
					
					filesPrecessedWriter.write(file_to_classify.getName()+"\n");
					filesPrecessedWriter.flush();
				}
			}
			filesPrecessedWriter.close();
		}  catch (Exception e) {
			taggingLog.error("Generic error in the classification step",e);
		}
	}
	/**
	 * 
	 * @param document
	 */
	private void tagging(Document document) {
		try {
			for (Sentence sentence : document.getSentences()) {
				//Set the hepatotoxicity terms present into the sentence
				findHepatotoxicityTerms(sentence);
				//Set the chemical compounds present into the sentence
				findChemicalCompounds(sentence);
				//Set the markers present into the sentence
				findMarkers(sentence);
				//Set the Cytochromes present into the sentence
				findCytochromes(sentence);
				//Set the species 
				//findSpecies(sentence);
				//Find relations between chemical compounds and hepatotoxicity terms in the sentence
				findChemicalCompoundHepatotoxicityRelations(sentence);
				//Find relations between chemical compounds and Cytochromes  in the sentence
				findChemicalCompoundCytochromeRelations(sentence);
				//Find relations between chemical compounds and Markers  in the sentence
				findMarkerChemicalCompoundRelations(sentence);
	    	}
		}  catch (Exception e) {
			taggingLog.error("Generic error in the tagging step",e);
		}
		
	}
	/**
	 * 
	 * @param file_to_classify
	 * @return
	 */
	private Map<String, Document> loadDocuments(File file_to_classify) {
		Map<String, Document> documents = new HashMap<String, Document>();
		taggingLog.info(" Load relevant documents in file " + file_to_classify.getAbsolutePath());
		try {
			for (String line : ObjectBank.getLineIterator(file_to_classify.getAbsolutePath(), "utf-8")) {
				String[] data = line.split("\t");
				String id =  data[3];
				String relevantLabel =  data[1];
				String paragraph =  data[5];
				Double score = new Double(data[0]);
				Document document = new Document(id, paragraph, score);
				documents.put(id, document);
			}
		} catch (Exception e) {
			taggingLog.error(" General Exception " + file_to_classify, e);
		}
		return documents;
	}
	/**
	 * 
	 * @param file_to_classify
	 * @return
	 */
	private void loadSentences(Map<String,Document> documents, File file_to_classify) {
		taggingLog.info(" Load relevant sentences in file " + file_to_classify.getAbsolutePath());
		for (String line : ObjectBank.getLineIterator(file_to_classify.getAbsolutePath(), "utf-8")) {
			String[] data = line.split("\t");
			String id_sentence =  data[3];
			String[] id = id_sentence.split("_");
			String docId = id[0];
			Integer order = new Integer(id[1]);
			String text =  data[4];
			Double score = new Double(data[0]);
			Document document =  documents.get(docId);
			if(document!=null) {
				Sentence sentence = new Sentence(id, order, text, score, document);
				document.getSentences().add(sentence);
			}else {
				taggingLog.info(" The sentence do not correspond to a paragraph id:  " + id_sentence );
			}
		}
	}	
	

	private List<String> readFilesProcessed(String outputDirectoryPath) {
		try {
			List<String> files_processed = new ArrayList<String>();
			if(Files.isRegularFile(Paths.get(outputDirectoryPath + File.separator + "list_files_processed.txt"))) {
				FileReader fr = new FileReader(outputDirectoryPath + File.separator + "list_files_processed.txt");
			    BufferedReader br = new BufferedReader(fr);
			    
			    String sCurrentLine;
			    while ((sCurrentLine = br.readLine()) != null) {
			    	files_processed.add(sCurrentLine);
				}
			    br.close();
			    fr.close();
			}
			return files_processed;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	


	/**
	  * Load Properties
	  * @param properitesParametersPath
	  */
	 public Properties loadPropertiesParameters(String properitesParametersPath) {
		 Properties prop = new Properties();
		 InputStream input = null;
		 try {
			 input = new FileInputStream(properitesParametersPath);
			 // load a properties file
			 prop.load(input);
			 return prop;
		 } catch (IOException ex) {
			 ex.printStackTrace();
		 } finally {
			 if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		}
		return null;
	 }
	 
	 	/**
		 * 
		 * @param document
		 * @param section
		 * @param sentence_text
		 * @param tokens
		 */
		private void findHepatotoxicityTerms(Sentence sentence) {
			for (HepatotoxicityTerm hepatotoxicityTerm : dictionaryService.getHepatotoxicityTerms()) {
				List<Ocurrence> ocurrences= sentenceContains(hepatotoxicityTerm.getOriginal_entry(), sentence.getText(), Constants.HEPATOTOXICITY);
				if(ocurrences!=null) {
					HepatotoxicityTermSentence hepatotoxicityTermSentence = new HepatotoxicityTermSentence(hepatotoxicityTerm,1f, ocurrences.size(), ocurrences, sentence);
					sentence.getHepatotoxicityTermSentences().add(hepatotoxicityTermSentence);
				}
			}
		}
		
		/**
		 * 
		 * @param document
		 * @param section
		 * @param sentence_text
		 * @param tokens
		 */
		private void findSpecies(Sentence sentence) {
			for (Taxonomy taxonomy : dictionaryService.getTaxonomies()) {
				List<Ocurrence> ocurrences = sentenceContains(taxonomy.getName(), sentence.getText(), Constants.SPECIES);
				if(ocurrences!=null) {
					TaxonomySentence taxonomySentence = new TaxonomySentence(taxonomy,1f, ocurrences.size(), ocurrences, sentence);
					sentence.getTaxonomySentences().add(taxonomySentence);
				}
			}
		}
		
		/**
		 * 
		 * @param document
		 * @param section
		 * @param sentence_text
		 * @param tokens
		 */
		private void findCytochromes(Sentence sentence) {
			for (Cytochrome cytochrome : dictionaryService.getCytochromes()) {
				this.findCytochromeByFieldType(cytochrome.getName(), sentence, cytochrome);
			}
		}
		
		/**
		 * 
		 * @param chemicalCompoundValue
		 * @param sentence_text
		 * @param document
		 * @param section
		 * @param chemicalCompound
		 */
		private void findCytochromeByFieldType(String cytochromeValue,Sentence sentence, Cytochrome cytochrome) {
			List<Ocurrence> ocurrences = sentenceContains(cytochromeValue, sentence.getText(), Constants.CYSP);
			if(ocurrences!=null) {
				CytochromeSentence cytochromeSentence = new CytochromeSentence(cytochrome, 1f , ocurrences.size(), ocurrences, sentence);
				sentence.getCytochromeSentences().add(cytochromeSentence);
			}
		}

		
		/**
		 * 
		 * @param chemicalCompoundValue
		 * @param sentence_text
		 * @param document
		 * @param section
		 * @param chemicalCompound
		 */
		private void findChemicalCompoundByFieldType(String chemicalCompoundValue, String chemicalCompoundValueType, 
				Sentence sentence, ChemicalCompound chemicalCompound) {
			List<Ocurrence> ocurrences = sentenceContains(chemicalCompoundValue, sentence.getText(), chemicalCompoundValueType);
			if(ocurrences!=null) {
				ChemicalCompoundSentence chemicalCompoundSentence = new ChemicalCompoundSentence(chemicalCompound, chemicalCompoundValueType,1f, ocurrences.size(), ocurrences, sentence);
				sentence.getChemicalCompoundSentences().add(chemicalCompoundSentence);
			}
		}
		
		/**
		 * 
		 * @param document_model
		 * @param sentence_model
		 * @param section
		 * @param sentence_text
		 * @param tokens
		 */
		private void findChemicalCompoundHepatotoxicityRelations(Sentence sentence) {
			for (HepatotoxicityTermSentence hepatotoxicityTermSentence : sentence.getHepatotoxicityTermSentences()) {
				for (ChemicalCompoundSentence chemicalCompoundSentence : sentence.getChemicalCompoundSentences()) {
					HepatotoxicityTermChemicalCompoundSentence hepatotoxicityTermChemicalCompoundSentence = 
							new HepatotoxicityTermChemicalCompoundSentence(chemicalCompoundSentence.getChemicalCompound(), hepatotoxicityTermSentence.getHepatotoxicityTerm(),1f, 1, sentence);
					sentence.getHepatotoxicityTermChemicalCompoundSentences().add(hepatotoxicityTermChemicalCompoundSentence);
					hepatotoxicityTermChemicalCompoundSentence.setRelationRule(RelationRule.COMENTION);
					for (ChemicalCompoundHepatotoxicityTermPattern pattern : dictionaryService.getChemicalCompoundHepatotoxicityTermPatterns()) {
						String pattern_text = pattern.getAdverse_pattern();
						pattern_text = pattern_text.replace("[substance]", chemicalCompoundSentence.getChemicalCompound().getName());
						pattern_text = pattern_text.replace("[adverse_effect]", hepatotoxicityTermSentence.getHepatotoxicityTerm().getOriginal_entry());
						List<Ocurrence> ocurrences = sentenceContains(pattern_text, sentence.getText(), Constants.ADVERSE_EFFECT);
						if(ocurrences!=null) {
							hepatotoxicityTermChemicalCompoundSentence.setRelationRule(RelationRule.ADVERSE_EFFECT);
							hepatotoxicityTermChemicalCompoundSentence.setPattern(pattern);
							break;
						}
					}
					taggingLog.debug("\t" + " " + "\t" + " " + "\t" +  chemicalCompoundSentence.getChemicalCompound().getName() + "\t" + hepatotoxicityTermSentence.getHepatotoxicityTerm().getOriginal_entry() + "\t" + " COMENTION ");
					//System.out.println("relation compound hepatotoxicityterm: " + chemicalCompoundSentence.getChemicalCompound().getName() + " and " + hepatotoxicityTermSentence.getHepatotoxicityTerm().getOriginal_entry());
				}	
				
			}	
		}
		
		/**
		 * Findings of ChemicalCompunds with synonyms
		 * @param sourceId
		 * @param document_model
		 * @param first_finding_on_document
		 * @param section
		 * @param sentence_text
		 * @return
		 * @throws MoreThanOneEntityException
		 */
		private void findChemicalCompounds(Sentence sentence) {
			for (ChemicalCompound chemicalCompound : dictionaryService.getChemicalCompounds()) {
				//Find and store differentes chemicalCompounds Key Words
				this.findChemicalCompoundByFieldType(chemicalCompound.getName(), Constants.CHEMICAL_NAME, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getSmile(), Constants.CHEMICAL_SMILE, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getInchi(), Constants.CHEMICAL_INCHI, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getChemPlusId(), Constants.CHEMICAL_CHEMPLUSID, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getChebi(), Constants.CHEMICAL_CHEBI, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getDrugBankId(), Constants.CHEMICAL_DRUGBANKID, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getKeggCompoundId(), Constants.KEGGCOMPOUNDID, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getMeshSubstanceId(), Constants.MESHID, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getCasRegistryNumber(), Constants.CASREGISTRYNUMBER, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getHumanMetabolome(), Constants.HUMANMETABOLOME, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getPubChemCompundId(), Constants.PUBCHEMID, sentence, chemicalCompound);
				this.findChemicalCompoundByFieldType(chemicalCompound.getPubChemSubstance(), Constants.PUBCHEMSUBSTANCE, sentence, chemicalCompound);
			}
		}
		
		/**
		 * 
		 * @param document_model
		 * @param sentence_model
		 * @param section
		 * @param sentence_text
		 * @param tokens
		 */
		private void findChemicalCompoundCytochromeRelations(Sentence sentence) {
			for (ChemicalCompoundSentence chemicalCompoundSentence : sentence.getChemicalCompoundSentences()) {
				for (CytochromeSentence cytochromeSentence : sentence.getCytochromeSentences()) {
					ChemicalCompoundCytochromeSentence chemicalCompoundCytochromeSentence = new ChemicalCompoundCytochromeSentence(chemicalCompoundSentence.getChemicalCompound(), cytochromeSentence.getCytochrome(),1f, 1, sentence);
					sentence.getChemicalCompoundCytochromeSentences().add(chemicalCompoundCytochromeSentence);
					chemicalCompoundCytochromeSentence.setRelationRule(RelationRule.COMENTION);
					Boolean cut = false;
					//Aca si leemos los patterns de induccion.
					for (CytochromeChemicalCompoundInductionPattern pattern : dictionaryService.getCytochromeChemicalCompoundInductionPatterns()) {
						String pattern_text = pattern.getCyp_induction_pattern();
						pattern_text = pattern_text.replace("[substance]", chemicalCompoundSentence.getChemicalCompound().getName());
						pattern_text = pattern_text.replace("[p450_cyps]", cytochromeSentence.getCytochrome().getName());
						List<Ocurrence> ocurrences = sentenceContains(pattern_text, sentence.getText(), Constants.CYPS_CHEM_PAT_IND);
						if(ocurrences!=null) {
							chemicalCompoundCytochromeSentence.setRelationRule(RelationRule.INDUCTION);
							chemicalCompoundCytochromeSentence.setPattern(pattern);
							cut=true;
							break;
						}
					}
					//Aca si leemos los patterns de inhibicion.
					if(!cut) {
						for (CytochromeChemicalCompoundInhibitionPattern pattern : dictionaryService.getCytochromeChemicalCompoundInhibitionPatterns()) {
							String pattern_text = pattern.getCyp_inhibition_pattern();
							pattern_text = pattern_text.replace("[substance]", chemicalCompoundSentence.getChemicalCompound().getName());
							pattern_text = pattern_text.replace("[p450_cyps]", cytochromeSentence.getCytochrome().getName());
							List<Ocurrence> ocurrences = sentenceContains(pattern_text, sentence.getText(), Constants.CYPS_CHEM_PAT_INH);
							if(ocurrences!=null) {
								chemicalCompoundCytochromeSentence.setRelationRule(RelationRule.INHIBITION);
								chemicalCompoundCytochromeSentence.setPattern(pattern);
								cut=true;
								break;
							}
						}
					}
					//Aca si leemos los patterns de metabolismo.
					if(!cut) {
						for (CytochromeChemicalCompoundMetabolismPattern pattern : dictionaryService.getCytochromeChemicalCompoundPatterns()) {
							String pattern_text = pattern.getSubstrate_pattern();
							pattern_text = pattern_text.replace("[substance]", chemicalCompoundSentence.getChemicalCompound().getName());
							pattern_text = pattern_text.replace("[p450_cyps]", cytochromeSentence.getCytochrome().getName());
							List<Ocurrence> ocurrences = sentenceContains(pattern_text, sentence.getText(), Constants.CYPS_CHEM_PAT_MET);
							if(ocurrences!=null) {
								chemicalCompoundCytochromeSentence.setRelationRule(RelationRule.METABOLISM);
								chemicalCompoundCytochromeSentence.setPattern(pattern);
								cut=true;
								break;
							}
						}
					}
					if(!cut) {
						taggingLog.debug("\t" + " " + "\t" + " " + "\t" +  chemicalCompoundSentence.getChemicalCompound().getName() + "\t" + cytochromeSentence.getCytochrome().getName() + "\t" + " COMENTION ");
						//System.out.println("\t" + " " + "\t" + " " + "\t" +  chemicalCompoundSentence.getChemicalCompound().getName() + "\t" + cytochromeSentence.getCytochrome().getName() + "\t" + " COMENTION ");
					}
					//Otra opcion seria verificar si entre medio hay palabras claves ... induction induce etc
					//Save Comention Compound and Cysp
					//System.out.println("relation compound-cys: " + chemicalCompoundSentence.getChemicalCompound().getName() + " and " + cytochromeSentence.getCytochrome().getName());
				}
			}
		}

		/**
		 * 
		 * @param document_model
		 * @param sentence_model
		 * @param section
		 * @param sentence_text
		 * @param tokens
		 */
		private void findMarkerChemicalCompoundRelations(Sentence sentence) {
			for (MarkerSentence markerSentence : sentence.getMarkerSentences()) {
				for (ChemicalCompoundSentence chemicalCompoundSentence : sentence.getChemicalCompoundSentences()) {
					MarkerChemicalCompoundSentence markerChemicalCompoundSentence = 
								new MarkerChemicalCompoundSentence(chemicalCompoundSentence.getChemicalCompound(), markerSentence.getMarker(),1f, 1, sentence, RelationRule.COMENTION);
					sentence.getMarkerChemicalCompoundSentences().add(markerChemicalCompoundSentence);
					Boolean cut = false;
					String type = "";
					/*for (MarkerChemicalCompoundPattern pattern : dictionaryService.getMarkerChemicalCompoundPatterns()) {
						String pattern_text = pattern.getMarker_pattern();
						type = pattern.getPattern_type();
						pattern_text = pattern_text.replace("[chemical]", chemicalCompoundSentence.getChemicalCompound().getName());
						pattern_text = pattern_text.replace("[marker]", markerSentence.getMarker().getMarker_full_name());
						//pattern_text = pattern_text.replace("number", markerSentence.getMarker().getMarker_full_name());
						int quantity_pattern = sentenceContains(pattern_text, sentence_text);
						if(quantity_pattern!=0) {
							if(type!=null && type.equals("Increase")) {
								markerChemicalCompoundSentence.setRelationRule(RelationRule.INCREASE);
								cut=true;
								System.out.println("pattern marker compound increase: " + pattern_text);
								break;
							}else if(type!=null && type.equals("Decrease")) {
								markerChemicalCompoundSentence.setRelationRule(RelationRule.DECREASE);
								cut=true;
								System.out.println("pattern marker compound decrease: " + pattern_text);
								break;
							}
						}
					}*/
					//Save Comention Compound and Cysp
					taggingLog.debug("\t" + " " + "\t" + " " + "\t" +  chemicalCompoundSentence.getChemicalCompound().getName() + "\t" + markerSentence.getMarker().getMarker_full_name() + "\t" + " COMENTION ");
					//System.out.println("compound marker: " + chemicalCompoundSentence.getChemicalCompound().getName() + " and " + markerSentence.getMarker().getMarker_full_name());
				}	
			}
		}
		/**
		 * 
		 * @param document
		 * @param section
		 * @param sentence_text
		 * @param tokens
		 */
		private void findMarkers(Sentence sentence) {
			for (Marker marker : dictionaryService.getMarkers()) {
				List<Ocurrence> ocurrences= sentenceContains(marker.getMarker_full_name(), sentence.getText(), Constants.MARKER);
				if(ocurrences!=null) {
					MarkerSentence markerSentence = new MarkerSentence(marker,1f, ocurrences.size(), ocurrences, sentence);
					sentence.getMarkerSentences().add(markerSentence);
				}
			}
		}
		
		/**
		 * 
		 * @param key_to_search
		 * @param sentence
		 * @return
		 */
		private List<Ocurrence> sentenceContains(String key_to_search, String sentence, String keyType) {
			if(key_to_search!=null && !key_to_search.trim().equals("") && !key_to_search.trim().equals(".")) {
				Pattern pattern = patterns.get(key_to_search);
				if(pattern==null) {
					String to_search = Pattern.quote(key_to_search);
			    	String patternString = "\\b(" + to_search + ")\\b";
				    pattern = Pattern.compile(patternString);
				    patterns.put(key_to_search, pattern);
				}
				Matcher m = pattern.matcher(sentence);
				Integer count = 0;
				List<Ocurrence> ocurrences = null;
				while (m.find()) {
					count++;
					if(count==1) {
						ocurrences = new ArrayList<Ocurrence>();
					}
					ocurrences.add(new Ocurrence(m.start(), m.end()));
					taggingLog.debug("\t" + m.start() + "\t" + m.end() + "\t" +  sentence.substring(m.start(), m.end()) + "\t" +  keyType);
					//relevantTerms.info(m.start() + "\t" + m.end() + "\t" +  sentence.substring(m.start(), m.end()) + "\t" +  keyType);
					//System.out.println("\t" + m.start() + "\t" + m.end() + "\t" +  sentence.substring(m.start(), m.end()));
				}
				return ocurrences;
			}
			return null;
		}
		
		
	  public void generateJSONFileForDocument(Document document, String outputPath) {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			objectMapper.setSerializationInclusion(Include.NON_EMPTY);
			try {
				objectMapper.writeValue(new File(outputPath), document);
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}	
	
	private void generateJSONFile(Collection<Document> documents, String outputPath)  {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		try {
			objectMapper.writeValue(new File(outputPath), documents);
		} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
