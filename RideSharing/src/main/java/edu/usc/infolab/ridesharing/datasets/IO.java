package edu.usc.infolab.ridesharing.datasets;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class IO {
	
	public static Document ReadXML(String filepath) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(filepath);		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return doc;		
	}
	
	public static void SaveXML(Document doc, String filepath){
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();

            StreamResult result = new StreamResult(new File(filepath));
            DOMSource source = new DOMSource(doc);
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            trans.transform(source, result);         
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	public static Document GetEmptyDoc() {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return doc;
	}

}
