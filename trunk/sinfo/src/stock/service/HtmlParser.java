package stock.service;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

public class HtmlParser {

	private TagNode nodes = null;
	private org.dom4j.Document htmldoc = null;
	Log log = LogFactory.getLog(this.getClass().getName());
	
	public   static Document docw3cToDom4j(org.w3c.dom.Document   doc)   throws   Exception   {     
		if   (doc   ==   null)   {     
			return   (null);     
		}     
		org.dom4j.io.DOMReader   xmlReader   =   new   org.dom4j.io.DOMReader();     
		return   xmlReader.read(doc);     
	}
	
	public HtmlParser(String htmlstr) {
		HtmlCleaner cleaner = new HtmlCleaner();
		try {
			this.nodes = cleaner.clean(htmlstr);
			CleanerProperties props = cleaner.getProperties();
			org.w3c.dom.Document docw3c;
			docw3c = new DomSerializer(props, true).createDOM(nodes);
			htmldoc = (org.dom4j.Document) docw3cToDom4j(docw3c);
			/*
			Node nodespec = htmldoc.selectSingleNode("/html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr/td[2]");
			if (nodespec != null)
				System.out.println(nodespec.getText());
			*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 
	public String getXpathSingleContent(String xpath) {
		try {
			// log.debug("reading xpath: "+xpath);
			Node nodespec = htmldoc.selectSingleNode(xpath);
			if (nodespec != null)
				return nodespec.getText();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("getXpathSingleContent: warning, null is getted!");
		return null;
	}

}
