/*******************************************************************************
 * Copyright (c) 2010 Octade Technologies  
 * 
 * This file is part of the Android IleSansFil Application project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  
 *******************************************************************************/
package com.octade.droid.ilesansfil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;


public class HotSpot implements Cloneable {

	static final String CURRENTMODULE = "IleSansFil.HotSpot";
	
	public static final String HOTSPOT_TAG="hotspot"; 
    public static final String HOTSPOTID_TAG = "hotspotId";
    public static final String NAME_TAG = "name";
    public static final String NODES_TAG = "nodes";
    public static final String OPENINGDATE_TAG = "openingDate"; 
    public static final String WEBSITEURL_TAG = "webSiteUrl"; 
    public static final String GLOBALSTATUS_TAG = "globalStatus"; 
    public static final String DESCRIPTION_TAG = "description"; 
    public static final String MASSTANSIFINFO_TAG = "massTransitInfo"; 
    public static final String CONTACTEMAIL_TAG = "contactEmail"; 
    public static final String CONTACTPHONENUMBER_TAG = "contactPhoneNumber"; 
    public static final String CIVICNUMBER_TAG = "civicNumber"; 
    public static final String STREETADDRESS_TAG = "streetAddress"; 
    public static final String CITY_TAG = "city"; 
    public static final String PROVINCE_TAG = "province"; 
    public static final String POSTALCODE_TAG = "postalCode"; 
    public static final String COUNTRY_TAG = "country"; 
    public static final String GISCENTERLATLONG_TAG = "gisCenterLatLong";
    public static final String LAT_ATTR_TAG = "lat";
    public static final String LONG_ATTR_TAG = "long";

    private ArrayList<HotSpotNode> nodes;
    
    private String hotspotId=""; 
    private String name="";
    private Date openingDate=new Date(); 
    private String webSiteUrl=""; 
    private int globalStatus=0; 
    private String description=""; 
    private String massTransitInfo=""; 
    private String contactEmail=""; 
    private String contactPhoneNumber=""; 
    private String civicNumber=""; 
    private String streetAddress=""; 
    private String city=""; 
    private String province=""; 
    private String postalCode=""; 
    private String country=""; 
    private double[] gisCenterLatLong={0.0,0.0};
    
    static SimpleDateFormat dateFormater=new SimpleDateFormat("yyyy-MM-dd");
  
    public HotSpot() {
    	clear();
    }
    
    public HotSpot(Node xmlNode) {
    	clear();
    	NodeList hotSpotProperties = xmlNode.getChildNodes();
        for (int j=0;j<hotSpotProperties.getLength();j++){
            Node property = hotSpotProperties.item(j);
            String nodeName = property.getNodeName();
            if (nodeName.equalsIgnoreCase(HOTSPOTID_TAG)){
                setHotspotId(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(NAME_TAG)){
                setName(property.getFirstChild().getNodeValue());               
            } else if (nodeName.equalsIgnoreCase(NODES_TAG)){
            	NodeList hotSpotNodesList = property.getChildNodes();
            	for(int k=0;k<hotSpotNodesList.getLength();k++){
            		nodes.add(new HotSpotNode(hotSpotNodesList.item(k)));
            	}
            } else if (nodeName.equalsIgnoreCase(OPENINGDATE_TAG)){
            	setOpeningDate(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(WEBSITEURL_TAG)){
                setWebSiteUrl(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(GLOBALSTATUS_TAG)){
                setGlobalStatus(Integer.parseInt(property.getFirstChild().getNodeValue()));
            } else if (nodeName.equalsIgnoreCase(MASSTANSIFINFO_TAG)){
                setMassTransitInfo(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(CONTACTEMAIL_TAG)){
                setContactEmail(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(CONTACTPHONENUMBER_TAG)){
                setContactPhoneNumber(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(CIVICNUMBER_TAG)){
                setCivicNumber(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(STREETADDRESS_TAG)){
                setStreetAddress(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(CITY_TAG)){
                setCity(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(PROVINCE_TAG)){
                setProvince(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(POSTALCODE_TAG)){
                setPostalCode(property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(GISCENTERLATLONG_TAG)){
                NamedNodeMap attributes = property.getFirstChild().getAttributes();
                gisCenterLatLong[0] = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
                gisCenterLatLong[1] = Double.parseDouble(attributes.getNamedItem("long").getNodeValue());
            } else if (nodeName.equalsIgnoreCase(DESCRIPTION_TAG)){
                StringBuilder text = new StringBuilder();
                NodeList chars = property.getChildNodes();
                for (int k=0;k<chars.getLength();k++){
                    text.append(chars.item(k).getNodeValue());
                }
                description = text.toString();
            }
        }
    }

    public void clear() {
    	name = "Unknown";
    	hotspotId = "-1";
    	nodes = new ArrayList<HotSpotNode>();
    	gisCenterLatLong = new double[2];
    	openingDate = new Date();
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result
				+ ((hotspotId == null) ? 0 : hotspotId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		HotSpot other = (HotSpot) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (hotspotId == null) {
			if (other.hotspotId != null)
				return false;
		} else if (!hotspotId.equals(other.hotspotId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getHotspotId() {
		return hotspotId;
	}

	public String getName() {
		return name;
	}

	public ArrayList<HotSpotNode> getNodes() {
		return nodes;
	}

	public Date getOpeningDate() {
		return openingDate;
	}

	public String getWebSiteUrl() {
		return webSiteUrl;
	}

	public int getGlobalStatus() {
		return globalStatus;
	}

	public String getDescription() {
		return description;
	}

	public String getMassTransitInfo() {
		return massTransitInfo;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public String getContactPhoneNumber() {
		return contactPhoneNumber;
	}

	public String getCivicNumber() {
		return civicNumber;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public String getCity() {
		return city;
	}

	public String getProvince() {
		return province;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getCountry() {
		return country;
	}

	public double getGisCenterLatLong(int index) {
		index &= 1;
		return gisCenterLatLong[index];
	}

	public void setHotspotId(String hotspotId) {
		this.hotspotId = hotspotId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOpeningDate(String openingDate) {
		try {
			this.openingDate = dateFormater.parse(openingDate);
		} catch (ParseException e) {
			Log.i(CURRENTMODULE+"setOpeningDate","Failed to Parse Date");
		}
	}

	public void setWebSiteUrl(String webSiteUrl) {
		this.webSiteUrl = webSiteUrl;
	}

	public void setGlobalStatus(int globalStatus) {
		this.globalStatus = globalStatus;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMassTransitInfo(String massTransitInfo) {
		this.massTransitInfo = massTransitInfo;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public void setCivicNumber(String civicNumber) {
		this.civicNumber = civicNumber;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setGisCenterLatLong(int index, double gisCenterLatLong) {
		index &=1;
		this.gisCenterLatLong[index] = gisCenterLatLong;
	}
	
	public HotSpotNode addNode() {
		HotSpotNode node = new HotSpotNode();
		nodes.add(node);
		return node;
	}
	
	public boolean delNode(HotSpotNode node) {
		if (nodes.contains(node)) {
			nodes.remove(node);
			return true;
		} 
		return false;
	}
	
	public void writeXml(XmlSerializer serializer)
	{
		try {
			
	        serializer.startTag("", HOTSPOT_TAG);
	        {
		        serializer.startTag("", HOTSPOTID_TAG);
		        serializer.text(getHotspotId());
		        serializer.endTag("", HOTSPOTID_TAG);
		        
		        serializer.startTag("", NAME_TAG);
		        serializer.text(getName());
		        serializer.endTag("", NAME_TAG);
		        
		        serializer.startTag("", OPENINGDATE_TAG);
		        serializer.text(getOpeningDate().toLocaleString());
		        serializer.endTag("", OPENINGDATE_TAG);
	
		        serializer.startTag("", WEBSITEURL_TAG);
		        serializer.text(getWebSiteUrl());
		        serializer.endTag("", WEBSITEURL_TAG);
	
		        serializer.startTag("", GLOBALSTATUS_TAG);
		        serializer.text(""+getGlobalStatus());
		        serializer.endTag("", GLOBALSTATUS_TAG);
	
		        serializer.startTag("", DESCRIPTION_TAG);
		        serializer.text(getDescription());
		        serializer.endTag("", DESCRIPTION_TAG);
		        
		        serializer.startTag("", MASSTANSIFINFO_TAG);
		        serializer.text(getMassTransitInfo());
		        serializer.endTag("", MASSTANSIFINFO_TAG);
	
		        serializer.startTag("", CONTACTEMAIL_TAG);
		        serializer.text(getContactEmail());
		        serializer.endTag("", CONTACTEMAIL_TAG);
	
		        serializer.startTag("", CONTACTPHONENUMBER_TAG);
		        serializer.text(getContactPhoneNumber());
		        serializer.endTag("", CONTACTPHONENUMBER_TAG);
		        
		        serializer.startTag("", CIVICNUMBER_TAG);
		        serializer.text(getCivicNumber());
		        serializer.endTag("", CIVICNUMBER_TAG);
		        
		        serializer.startTag("", STREETADDRESS_TAG);
		        serializer.text(getStreetAddress());
		        serializer.endTag("", STREETADDRESS_TAG);
		        
		        serializer.startTag("", CITY_TAG);
		        serializer.text(getCity());
		        serializer.endTag("", CITY_TAG);
		        
		        serializer.startTag("", PROVINCE_TAG);
		        serializer.text(getProvince());
		        serializer.endTag("", PROVINCE_TAG);
		        
		        serializer.startTag("", POSTALCODE_TAG);
		        serializer.text(getPostalCode());
		        serializer.endTag("", POSTALCODE_TAG);
		        
		        serializer.startTag("", COUNTRY_TAG);
		        serializer.text(getCountry());
		        serializer.endTag("", COUNTRY_TAG);
		        
		        serializer.startTag("", GISCENTERLATLONG_TAG);
	            serializer.attribute("", LAT_ATTR_TAG, ""+getGisCenterLatLong(0));
		        serializer.attribute("", LONG_ATTR_TAG, ""+getGisCenterLatLong(1));
		        serializer.endTag("", GISCENTERLATLONG_TAG);
	    
		        serializer.startTag("", NODES_TAG);
		        	for (int i=0;i<nodes.size();i++) {
		        		nodes.get(i).writeXml(serializer);
		        	}
		        serializer.endTag("", NODES_TAG);
			}   
	        
/*
              serializer.startTag("", "message");
	           serializer.attribute("", "date", msg.getDate());
        	   serializer.text(msg.getContent());
	           serializer.endTag("", "message");
*/
	        serializer.endTag("", HOTSPOT_TAG);
	        return ;
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }		
	
	}
	@Override
	@SuppressWarnings("unchecked")
	public Object clone()   {
		try { 
			HotSpot result = (HotSpot) super.clone();
			result.nodes.clear();
			for(Iterator i=nodes.iterator();i.hasNext();) {
				HotSpotNode node = (HotSpotNode) i.next(); 
				result.nodes.add((HotSpotNode) node.clone());
			}
			result.gisCenterLatLong = gisCenterLatLong.clone();
			result.openingDate = (Date) openingDate.clone();
			return result;
		} catch( CloneNotSupportedException e) {
			e.printStackTrace();
			return new HotSpot();
		}
	 }
}
