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
import java.util.Date;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;

public class HotSpotNode implements Cloneable {
	
	static final String CURRENTMODULE = "IleSansFil.HotSpotNode";

	enum NodeStatus {down,up};
	
	public static final String NODE_TAG = "node"; 
	public static final String NODEID_TAG = "nodeId"; 
	public static final String NUMONLINEUSERS_TAG = "numOnlineUsers"; 
	public static final String CREATIONDATE_TAG = "creationDate"; 
	public static final String STATUS_TAG = "status"; 
	public static final String GISLATLONG_TAG = "gisLatLong";
	public static final String LAT_ATTR_TAG = "lat";
	public static final String LONG_ATTR_TAG = "long";
    
	private String nodeId=""; 
	private int numOnlineUsers=0; 
	private Date creationDate = new Date(); 
	private NodeStatus status=NodeStatus.down;
	private double[] gisLatLong={0.0,0.0};
	
	
    
	public HotSpotNode () {
		
	}
	
    public HotSpotNode (Node xmlNode) {
    	NodeList hostSpotNodeProperties = xmlNode.getChildNodes();
        for (int j=0;j<hostSpotNodeProperties.getLength();j++){
            Node property = hostSpotNodeProperties.item(j);
            String nodeName = property.getNodeName();
            if (nodeName.equalsIgnoreCase(NODEID_TAG)){
                nodeId = property.getFirstChild().getNodeValue();
            } else if (nodeName.equalsIgnoreCase(NUMONLINEUSERS_TAG)){
                numOnlineUsers = Integer.parseInt(property.getFirstChild().getNodeValue());               
            } else if (nodeName.equalsIgnoreCase(CREATIONDATE_TAG)){
                creationDate.setTime(Date.parse(property.getFirstChild().getNodeValue()));               
            } else if (nodeName.equalsIgnoreCase(STATUS_TAG)){
               	setStatus (property.getFirstChild().getNodeValue());
            } else if (nodeName.equalsIgnoreCase(GISLATLONG_TAG)){
            	NamedNodeMap attributes = property.getFirstChild().getAttributes();
                setGisLatLong(0, Double.parseDouble(attributes.getNamedItem("lat").getNodeValue()));
                setGisLatLong(1, Double.parseDouble(attributes.getNamedItem("long").getNodeValue()));
            }
        }
    }
    public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setCreationDate(String creationDate) {
		try {
			this.creationDate = HotSpot.dateFormater.parse(creationDate);
		} catch (ParseException e) {
			Log.i(CURRENTMODULE+"setCreationDate","Failed to Parse Date");
		}
	}

	public String getNodeId() {
		return nodeId;
	}

	public int getNumOnlineUsers() {
		return numOnlineUsers;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public double getGisLatLong(int index) {
		index &=1;
		return gisLatLong[index];
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public void setNumOnlineUsers(int numOnlineUsers) {
		this.numOnlineUsers = numOnlineUsers;
	}
	public void setStatus(NodeStatus status) {
		this.status = status;
	}
	public void setStatus(String status) {
        if(status.equalsIgnoreCase("up")) {
        	setStatus (NodeStatus.up);
        } else {
        	setStatus (NodeStatus.down);
        }
	}
	public void setGisLatLong(int index , double gisLatLong) {
		index &= 1;
		this.gisLatLong[index] = gisLatLong;
	}
	
	public void writeXml(XmlSerializer serializer) {
        try {
            serializer.startTag("", NODE_TAG);
            {
		        serializer.startTag("", GISLATLONG_TAG);
	            serializer.attribute("", LAT_ATTR_TAG, ""+getGisLatLong(0));
		        serializer.attribute("", LONG_ATTR_TAG, ""+getGisLatLong(1));
		        serializer.endTag("", GISLATLONG_TAG);
	
		        serializer.startTag("", CREATIONDATE_TAG);
		        serializer.text(getCreationDate().toLocaleString());
		        serializer.endTag("", CREATIONDATE_TAG);
	
		        serializer.startTag("", NODEID_TAG);
		        serializer.text(getNodeId());
		        serializer.endTag("", NODEID_TAG);
		        
		        serializer.startTag("", NUMONLINEUSERS_TAG);
		        serializer.text(""+getNumOnlineUsers());
		        serializer.endTag("", NUMONLINEUSERS_TAG);
		        
		        serializer.startTag("", STATUS_TAG);
		        serializer.text(""+getStatus());
		        serializer.endTag("", STATUS_TAG);
            }
            serializer.endTag("", NODE_TAG);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}

	@Override
	protected Object clone()  {
		try { 
			HotSpotNode result = (HotSpotNode) super.clone();
			result.gisLatLong = gisLatLong.clone();
			result.creationDate = (Date) creationDate.clone();
			return result;
		} catch( CloneNotSupportedException e) {
			e.printStackTrace();
			return new HotSpotNode();
		}	}
}
