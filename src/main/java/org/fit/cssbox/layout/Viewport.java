/*
 * Viewport.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * CSSBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * CSSBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 */

package org.fit.cssbox.layout;

import java.awt.*;
import java.util.Vector;

import org.fit.cssbox.render.BoxRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import cz.vutbr.web.css.CSSFactory;

/**
 * This class represents a browser viewport which is implemented as a special case of a block
 * box. It differs mainly in the way the sizes are computed. Moreover, it provides the methods
 * for margin collapsing which is done before the layout itself.
 * 
 * @author radek
 */
public class Viewport extends BlockBox
{
    private static Logger log = LoggerFactory.getLogger(Viewport.class);
    
	private int width;
	private int height;
	protected BrowserConfig config;
	
	private BoxFactory factory;
	private BoxRenderer renderer;
	private Element root; //the DOM root
	private ElementBox rootBox; //the box that corresponds to the root node. It should be one of the child boxes.
    protected ElementBox lastbox = null;
    protected ElementBox lastparent = null;
    private int maxx; //maximal X position of all the content
    private int maxy; //maximal Y position of all the content

    
    public Viewport(Element e, Graphics2D g, VisualContext ctx, BoxFactory factory, Element root, int width, int height)
	{
		super(e, g, ctx);
		this.factory = factory;
		this.root = root;
		style = CSSFactory.createNodeData(); //Viewport starts with an empty style
        nested = new Vector<Box>();
        startChild = 0;
        endChild = 0;
		this.width = width;
		this.height = height;
        isblock = true;
        contblock = true;
        root = null;
	}
    
    /**
     * Obtains the current browser configuration.
     * @return current configuration.
     */
    public BrowserConfig getConfig()
    {
        return config;
    }

    /**
     * Sets the browser configuration used for rendering.
     * @param config the new configuration.
     */
    public void setConfig(BrowserConfig config)
    {
        this.config = config;
    }
    
    @Override
    public void initSubtree()
    {
        super.initSubtree();
        loadBackgroundFromContents();
    }
    
    @Override
    public String toString()
    {
        return "Viewport " + width + "x" + height;
    }
    
    public BoxFactory getFactory()
    {
        return factory;
    }

    public int getMinimalWidthLimit()
    {
    	return width;
    }
    
    /**
     * Obtains the DOM root element.
     * @return The root element of the document.
     */
    public Element getRootElement()
    {
        return root;
    }

    /**
     * Obtains the child box the corresponds to the DOM root element.
     * @return the corresponding element box or <code>null</code> if the viewport is empty.
     */
    public ElementBox getRootBox()
    {
        return rootBox;
    }
    
    public ElementBox getElementBoxByName(String name, boolean case_sensitive)
    {
        return recursiveFindElementBoxByName(this, name, case_sensitive);
    }
    
	@Override
    public void addSubBox(Box box)
    {
        super.addSubBox(box);
        if (box instanceof ElementBox && ((ElementBox) box).getElement() == root)
        {
            if (rootBox != null)
                log.debug("Viewport warning: another root box '" + box + "' in addition to previous '" + rootBox + "'");
            box.makeRoot();
            rootBox = (ElementBox) box;
        }
    }

    @Override
	public boolean hasFixedHeight()
	{
		return false;
	}

	@Override
	public boolean hasFixedWidth()
	{
		return false;
	}

	@Override
    public boolean canIncreaseWidth()
    {
        return true;
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    protected boolean separatedFromTop(ElementBox box)
    {
        return true;
    }

    @Override
    protected boolean separatedFromBottom(ElementBox box)
    {
        return true;
    }

	@Override
	public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
        content = new Dimension(width, height);
        bounds = new Rectangle(0, 0, totalWidth(), totalHeight());
    }
    
	@Override
	public Viewport getViewport()
	{
		return this;
	}

    @Override
    protected void loadPosition()
    {
        position = BlockBox.POS_ABSOLUTE;
        topset = true;
        leftset = true;
        bottomset = false;
        rightset = false;
        coords = new LengthSet(0, 0, 0, 0);
    }
    
	@Override
	protected void loadSizes(boolean update)
	{
		if (!update)
		{
			margin = new LengthSet();
			emargin = new LengthSet();
			declMargin = new LengthSet();
			border = new LengthSet();
			padding = new LengthSet();
			content = new Dimension(width, height);
			min_size = new Dimension(-1, -1);
			max_size = new Dimension(-1, -1);
			loadPosition();
		}
		bounds = new Rectangle(0, 0, totalWidth(), totalHeight());
	}

	@Override
    public void setContentWidth(int width)
    {
	    //do not descrease the viewport width under the initial value
        if (width > this.width)
            super.setContentWidth(width);
    }

    @Override
    public void setContentHeight(int height)
    {
        //do not descrease the viewport height under the initial value
        if (height > this.height)
            super.setContentHeight(height);
    }

    @Override
    protected void loadBackground()
    {
	    bgcolor = null; //during the initialization, the background is not known
    }

	/**
	 * Use the root box or the body box (for HTML documents) for obtaining the backgrounds.
	 */
	private void loadBackgroundFromContents()
	{
	    if (rootBox != null)
	    {
    	    ElementBox src = rootBox;
    	    if (src.getBgcolor() == null && config.getUseHTML()) //for HTML, try to use the body
    	        src = getElementBoxByName("body", false);
    	    
    	    if (src != null)
    	    {
        	    if (src.getBgcolor() != null)
        	    {
        	        bgcolor = src.getBgcolor();
        	        src.setBgcolor(null);
        	    }
        	    if (src.getBackgroundImages() != null && !src.getBackgroundImages().isEmpty())
        	    {
        	        bgimages = loadBackgroundImages(src.getStyle());
        	        src.getBackgroundImages().clear();
        	    }
    	    }
    	    else
    	        log.debug("Couldn't find the HTML <body> element");
	    }
	}
	
    /**
	 * Calculates the absolute positions and updates the viewport size
	 * in order to enclose all the boxes.
	 * @param min the minimal viewport dimensions
	 */
	public void updateBounds(Dimension min)
	{
		//first round - compute the viewport size
		maxx = min.width;
		maxy = min.height;
		for (int i = 0; i < getSubBoxNumber(); i++)
			getSubBox(i).absolutePositions();
		//update the size
		if (width < maxx) width = maxx;
		if (height < maxy) height = maxy;
		loadSizes();
		loadBackgroundFromContents(); //the background image positions may have changed
	}

    @Override
    public boolean doLayout(int availw, boolean force, boolean linestart)
    {
        //remove previously splitted children from possible previous layout
        clearSplitted();

        //viewport has a siplified width computation algorithm
        int min = getMinimalContentWidth();
        int pref = Math.max(min, width);
        setContentWidth(pref);
        updateChildSizes();
        
        //the width should be fixed from this point
        widthComputed = true;
        
        /* Always try to use the full width. If the box is not in flow, its width
         * is updated after the layout */
        setAvailableWidth(totalWidth());
        
        if (!contblock)  //block elements containing inline elements only
            layoutInline();
        else //block elements containing block elements
            layoutBlocks();
        
        //allways fits as well possible
        return true;
    }
	
    @Override
    public boolean formsStackingContext()
    {
        return true;
    }
    
	@Override
    public void absolutePositions()
    {
	    if (parent == null) //viewport may be the root
	        absbounds = new Rectangle(bounds);
	    else //or a nested viewport
	        absbounds = new Rectangle(parent.getAbsoluteContentBounds());
	    
	    //clear this context if it exists (remove old children)
	    if (scontext != null)
            scontext.clear();
	    
		for (int i = 0; i < getSubBoxNumber(); i++)
			getSubBox(i).absolutePositions();
    }
	
    /**
     * Sets the current renderer and draws the whole subtree using the given renderer.
     * @param g The graphic context to be used for painting.
     */
    public void draw(BoxRenderer renderer)
    {
        this.renderer = renderer;
        drawStackingContext(false);
    }
	
    /**
     * Obtains the current renderer used for painting the boxes.
     * @return current renderer.
     */
    public BoxRenderer getRenderer()
    {
        return renderer;
    }
    
	/**
	 * Updates the maximal viewport size according to the element bounds
	 */
	public void updateBoundsFor(Rectangle bounds)
	{
		int x = bounds.x + bounds.width - 1;
		if (maxx < x) maxx = x;
		int y = bounds.y + bounds.height - 1;
		if (maxy < y) maxy = y;
	}
	
	/**
	 * Uses the given block as a clipping block instead of the default Viewport.
	 * @param block the new clipping block
	 */
	public void clipByBlock(BlockBox block)
	{
	    recursivelySetClipBlock(this, block);
	}
	
	private void recursivelySetClipBlock(Box root, BlockBox clip)
	{
	    if (root == this || root.getClipBlock() == this)
	    {
	        root.setClipBlock(clip);
	        if (root instanceof ElementBox)
	        {
	            ElementBox eb = (ElementBox) root;
	            for (int i = eb.getStartChild(); i < eb.getEndChild(); i++)
	                recursivelySetClipBlock(eb.getSubBox(i), clip);
	        }
	    }
	}
	
    //===================================================================================
    
    private ElementBox recursiveFindElementBoxByName(ElementBox ebox, String name, boolean case_sensitive)
    {
        boolean eq;
        if (case_sensitive)
            eq = ebox.getElement().getTagName().equals(name);
        else
            eq = ebox.getElement().getTagName().equalsIgnoreCase(name);
        
        if (eq)
            return ebox;
        else
        {
            ElementBox ret = null;
            for (int i = 0; i < ebox.getSubBoxNumber() && ret == null; i++)
            {
                Box child = ebox.getSubBox(i);
                if (child instanceof ElementBox)
                    ret = recursiveFindElementBoxByName((ElementBox) child, name, case_sensitive);
            }
            return ret;
        }
    }

}

