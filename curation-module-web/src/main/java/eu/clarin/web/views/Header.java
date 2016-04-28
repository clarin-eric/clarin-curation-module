package eu.clarin.web.views;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.declarative.Design;


@DesignRoot
public class Header extends Panel{
	
	Link acdh;
	Link clarin;
 
	public Header(){
		Design.read(this);
		
		acdh.setIcon(new ThemeResource("img/acdh-logo.png"));
		clarin.setIcon(new ThemeResource("img/clarin-logo.png"));
	}
}