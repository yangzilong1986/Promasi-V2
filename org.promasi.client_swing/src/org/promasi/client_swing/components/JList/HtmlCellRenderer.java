/**
 * 
 */
package org.promasi.client_swing.components.JList;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;

import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import org.promasi.client_swing.components.JEditorPane.ExtendedJEditorPane;

/**
 * @author alekstheod
 *
 */
public class HtmlCellRenderer extends DefaultListCellRenderer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	private JPanel _mainPanel;
	
	/**
	 * 
	 */
	private ExtendedJEditorPane _htmlPane;
	
	/**
	 * 
	 */
	public HtmlCellRenderer(){
		_mainPanel = new JPanel();
		_mainPanel.setLayout( new MigLayout( new LC( ).fill( ) ));
		_mainPanel.setBorder( BorderFactory.createEtchedBorder( ) );
		_htmlPane = new ExtendedJEditorPane();
		_htmlPane.setEditable(false);
		_htmlPane.setContentType("text/html" );
		_htmlPane.setPreferredSize(new Dimension(100,200));
		
		_htmlPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		_htmlPane.setVisible(true);
		_htmlPane.setFocusable(true);
		_htmlPane.setAutoscrolls(true);
		_mainPanel.add(_htmlPane, new CC( ).spanX( ).grow( ).gapX( "30px", "0px" ));
	}
	
	/**
	 * 
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
		_htmlPane.setText( value.toString() );
		if( isSelected ){
			_mainPanel.setBackground(Color.LIGHT_GRAY);
		}else{
			_mainPanel.setBackground(Color.WHITE);
		}
		
		return _mainPanel;
	}
}
