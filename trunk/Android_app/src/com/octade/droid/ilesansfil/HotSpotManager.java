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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;
import android.content.Context;

//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

public class HotSpotManager  {

	static final String CURRENTMODULE = "IleSansFil.HotSpotManager";

	public enum statusFormat {xml};

	private static final String NETWORKMETADATA_TAG="networkMetadata"; 
	private static final String NETWORKURI_TAG="networkUri"; 
	private static final String NAME_TAG="name"; 
	private static final String WEBSITEURL_TAG="websiteUri"; 
	private static final String HOTSPOTSCOUNT_TAG="hotspotsCount"; 
	private static final String VALIDSUBSCRIBEDUSERSCOUNT_TAG="validSubscribedUsersCount"; 
//	private static final String ONLINEUSERSCOUNT_TAG="onlineUsersCount";
	private static final String HOTSPOTS_TAG="hotspots"; 
	private static final String CACHEFILENAME="hotspots.xml";

	private String statusURL;
	private statusFormat format;
	
	private IleSansFilApp mainApp;
	
	private String networkUri="";
	private String name=""; 
	private String websiteUrl="";
	private int hotspotsCount=0; 
	private int validSubscribedUsersCount=0; 
	private int onlineUsersCount=0;

	private ArrayList<HotSpot> hotSpots = new ArrayList<HotSpot>();
	
	public HotSpotManager(IleSansFilApp mainApp, String statusURL,statusFormat format) {
		this.mainApp = mainApp;
		init(statusURL,format);
	}
	
	public HotSpotManager(String statusURL) {
		init(statusURL,statusFormat.xml);
	}
	
	private void init(String statusURL,statusFormat format) {
		this.statusURL = statusURL;
		this.format = format;
		hotSpots.clear();
	}

	
	// XML Parser temporary variables
	private HotSpot currentXmlHotSpot ;
	private HotSpotNode currentXmlHotSpotNode;
	
/*SSL Hack from http://stackoverflow.com/questions/995514/https-connection-android
 * 	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
                return true;
        }
	};

	private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { 
        		new X509TrustManager() {
        			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
        			}

        			public void checkClientTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
        			}

        			public void checkServerTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
        			}
        		}
          };

        // Install the all-trusting trust manager
        try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
                e.printStackTrace();
        }
	}
*/
	
	public void loadHotSpotsSax(boolean fromCache) {
		boolean createCache=false;
		InputStream iStream=null;
		URLConnection uc=null;
    	File f = mainApp.getApplicationContext().getFileStreamPath(CACHEFILENAME);
    	Log.i(CURRENTMODULE,"CacheModified: "+(new Date(f.lastModified())).toLocaleString());
    	Date now = new Date();
    	Log.i(CURRENTMODULE,"Now : "+now.toLocaleString());
    	long cacheAge = now.getTime()-f.lastModified();
		if(cacheAge < 1000*3600*2) fromCache=true;
		try {
			URL url = new URL(statusURL);
			System.setProperty("http.KeepAlive","false");
			if (mainApp.isNetworkAvailable() && !fromCache) { 
				if (url.getProtocol().toLowerCase().equals("https")) {
					HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
					https.connect();
					uc=https;
	        	} else {
					uc = (URLConnection) url.openConnection();
	        	}
			}
            RootElement root = new RootElement("wifidogHotspotsStatus");
            
            root.setStartElementListener(new StartElementListener() {
				@Override
				public void start(Attributes attributes) {
					Log.i(CURRENTMODULE, "Start Parsing");
				}           
            });
            
			Element netMetaElement = root.getChild(NETWORKMETADATA_TAG);
			
			netMetaElement.getChild(NETWORKURI_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                setNetworkUri (body);
	            }
			});
			
			netMetaElement.getChild(NAME_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                setName (body);
	            }
			});
			
			netMetaElement.getChild(WEBSITEURL_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                setWebsiteUrl(body);
	            }
			});
			netMetaElement.getChild(HOTSPOTSCOUNT_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                setHotspotsCount(Integer.parseInt(body));
	            }
			});
			netMetaElement.getChild(VALIDSUBSCRIBEDUSERSCOUNT_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                setValidSubscribedUsersCount(Integer.parseInt(body));
	            }
			});
			Element hotSpotsElement = root.getChild(HOTSPOTS_TAG); 
			hotSpotsElement.setStartElementListener(new StartElementListener() {

				@Override
				public void start(Attributes arg0) {
					Log.i(CURRENTMODULE,"HotSpots");
				}
			});
			Element hotSpotItem = hotSpotsElement.getChild(HotSpot.HOTSPOT_TAG);
		    Element hotSpotNodeItem = hotSpotItem.getChild(HotSpotNode.NODE_TAG);

		    hotSpotItem.setElementListener(new ElementListener() {
				public void start(Attributes attributes) {
					currentXmlHotSpot = new HotSpot();				
				}
				public void end() {
	                addHotSpot(currentXmlHotSpot);
				}
		    });

		    hotSpotItem.getChild(HotSpot.HOTSPOTID_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setHotspotId(body);
				}
		    });
		    hotSpotItem.getChild(HotSpot.NAME_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setName(body);
				}
		    });
		    hotSpotItem.getChild(HotSpot.OPENINGDATE_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setOpeningDate(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.WEBSITEURL_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setWebSiteUrl(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.GLOBALSTATUS_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setGlobalStatus(Integer.parseInt(body));
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setDescription(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.MASSTANSIFINFO_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setMassTransitInfo(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.CONTACTEMAIL_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setContactEmail(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.CONTACTPHONENUMBER_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setContactPhoneNumber(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.CIVICNUMBER_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setCivicNumber(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.STREETADDRESS_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setStreetAddress(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.CITY_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setCity(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.PROVINCE_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setProvince(body);
				}
		    });		    
		    hotSpotItem.getChild(HotSpot.POSTALCODE_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setPostalCode(body);
				}
		    }); 
		    hotSpotItem.getChild(HotSpot.COUNTRY_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpot.setCountry(body);
				}
		    }); 		    
		    hotSpotItem.getChild(HotSpot.GISCENTERLATLONG_TAG).setStartElementListener(new StartElementListener() {
				public void start(Attributes attributes) {
					Double lt=0.0,lg=0.0;
					try {
						if(attributes.getValue(HotSpot.LAT_ATTR_TAG).length()!=0) {
							lt= Double.parseDouble(attributes.getValue(HotSpot.LAT_ATTR_TAG));
							lg= Double.parseDouble(attributes.getValue(HotSpot.LONG_ATTR_TAG));
						} else {
							lt =lg =0.0;
						}
					}catch(NumberFormatException e) {
						Log.i(CURRENTMODULE,"Error parsing HotSpot GIS Coordinates");
					}
					currentXmlHotSpot.setGisCenterLatLong(0,lt);
					currentXmlHotSpot.setGisCenterLatLong(1,lg);
				}
		    });
		    
		    hotSpotItem.getChild(HotSpot.NODES_TAG).setStartElementListener(new StartElementListener() {
				public void start(Attributes attributes) {
					currentXmlHotSpotNode = currentXmlHotSpot.addNode();
				}
		    });		    
		    hotSpotNodeItem.getChild(HotSpotNode.GISLATLONG_TAG).setStartElementListener(new StartElementListener() {
				public void start(Attributes attributes) {
					Double lt=0.0,lg=0.0;
					try {
						if(attributes.getValue(HotSpot.LAT_ATTR_TAG).length()!=0) {
							lt= Double.parseDouble(attributes.getValue(HotSpot.LAT_ATTR_TAG));
							lg= Double.parseDouble(attributes.getValue(HotSpot.LONG_ATTR_TAG));
						} else {
							lt =lg =0.0;
						}
					}catch(NumberFormatException e) {
						Log.i(CURRENTMODULE,"Error parsing Node GIS Coordinates "+e.toString());
					}
					currentXmlHotSpotNode.setGisLatLong(0,lt);
					currentXmlHotSpotNode.setGisLatLong(1,lg);
				
				}
		    });		    
		    hotSpotNodeItem.getChild(HotSpotNode.CREATIONDATE_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpotNode.setCreationDate(body);
				}
		    }); 
		    hotSpotNodeItem.getChild(HotSpotNode.NODEID_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpotNode.setNodeId(body);
				}
		    }); 
		    hotSpotNodeItem.getChild(HotSpotNode.NUMONLINEUSERS_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpotNode.setNumOnlineUsers(Integer.parseInt(body));
				}
		    }); 
		    hotSpotNodeItem.getChild(HotSpotNode.STATUS_TAG).setEndTextElementListener(new EndTextElementListener() {
				public void end(String body) {
	                currentXmlHotSpotNode.setStatus(body);
				}
		    });
		    
		    if(uc!=null) {
		    	// We have a network Connection
		    	try {
		    		iStream = uc.getInputStream();
		    		createCache=true;
		    	} catch (SecurityException e) {
		    		e.printStackTrace();
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    		Log.i(CURRENTMODULE, "Cannot connect to hotSpot Status");
		    	} 
		    }
		    if(iStream == null) {
		    	// no Network Available or Server Down or requested to load from cache
		    	iStream =  mainApp.getApplicationContext().openFileInput(CACHEFILENAME);
		    }
		    
        	Xml.parse(iStream , Xml.Encoding.UTF_8, root.getContentHandler());
        	// Try to do some sort of Sorting 
        	Collections.sort(hotSpots, new Comparator<HotSpot>() {
        		 public int compare(HotSpot o1, HotSpot o2) {
        			 if (o2.getGisCenterLatLong(0) > o1.getGisCenterLatLong(0)) {
        			 	return 1;
        			 } else if (o2.getGisCenterLatLong(0) == o1.getGisCenterLatLong(0)) {
        			 	if (o2.getGisCenterLatLong(1) > o1.getGisCenterLatLong(1)) return 1;
        			 	if (o2.getGisCenterLatLong(1) == o1.getGisCenterLatLong(1)) return 0;
        			 	return -1;
        			 } else {
        			 	return -1;
        			 }
        		 }
        	});
        	// Create local cache to offline loading
       		if(createCache) writeXml();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
/*
	public void loadHotSpotsDom()  {
		Log.i(CURRENTMODULE,"loadHotSpotsDom");
		if (!mainApp.isNetworkAvailable()) return;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			URL url = new URL(statusURL);
			URLConnection uc = url.openConnection();
			db = dbf.newDocumentBuilder();
			Document doc=db.newDocument();
			try {
				 doc = db.parse(uc.getInputStream());
			} catch (DOMException e) {
				e.printStackTrace();
			}
			doc.getDocumentElement().normalize();
			Log.i(CURRENTMODULE,"Status Root:"+doc.getDocumentElement().getNodeName());
			NodeList metaDataNodes = doc.getElementsByTagName(NETWORKMETADATA_TAG);
			if(metaDataNodes.getLength()>=1) {
				NodeList metaDataProperties = metaDataNodes.item(0).getChildNodes();
				for(int i=0;i<metaDataProperties.getLength();i++) {
					Node property = metaDataProperties.item(i);
		            String nodeName = property.getNodeName();
		            if (nodeName.equalsIgnoreCase(NETWORKURI_TAG)){
		                setNetworkUri(property.getFirstChild().getNodeValue());
		            } else if (nodeName.equalsIgnoreCase(NAME_TAG)){
		                name = property.getFirstChild().getNodeValue();               
		            } else if (nodeName.equalsIgnoreCase(WEBSITEURL_TAG)){
		                websiteUrl = property.getFirstChild().getNodeValue();               
		            } else if (nodeName.equalsIgnoreCase(HOTSPOTSCOUNT_TAG)){
		                hotspotsCount = Integer.parseInt(property.getFirstChild().getNodeValue());               
		            } else if (nodeName.equalsIgnoreCase(VALIDSUBSCRIBEDUSERSCOUNT_TAG)){
		                validSubscribedUsersCount = Integer.parseInt(property.getFirstChild().getNodeValue());               
		            } else if (nodeName.equalsIgnoreCase(ONLINEUSERSCOUNT_TAG)){
		                onlineUsersCount = Integer.parseInt(property.getFirstChild().getNodeValue());               
		            } 
				}
			} else {
				Log.i(CURRENTMODULE,"Could not find networkMetaData in XML Document");
			}
			NodeList hotSpotsNodes = doc.getElementsByTagName(HotSpot.HOTSPOT_TAG);
			if(hotSpotsNodes.getLength()>=1) {
				hotSpots = new ArrayList<HotSpot>();
				for(int i=0;i<hotSpotsNodes.getLength();i++) {
					hotSpots.add(new HotSpot(hotSpotsNodes.item(i)));
				}
				
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated Parser Configuration catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated DOM catch block			
		} catch (SAXException e) {
			// TODO Auto-generated SAX catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated Other catch block
			e.printStackTrace();
		}
		
	}
*/	
	private void writeXml() {
		
		XmlSerializer serializer=Xml.newSerializer();
	    try {
	    	FileOutputStream output = mainApp.getApplicationContext().openFileOutput(CACHEFILENAME, Context.MODE_WORLD_WRITEABLE);
	    	if(output==null) return;
	    	//OutputStreamWriter writer = new OutputStreamWriter(output);
	    	//if(writer==null) return;
	        serializer.setOutput(output,"UTF-8");
	        serializer.startDocument("UTF-8", true);
	        serializer.startTag("", "wifidogHotspotsStatus");
	        
	        serializer.startTag("", NETWORKMETADATA_TAG);

	        	serializer.startTag("", NETWORKURI_TAG);
	        	serializer.text(getNetworkUri());
	        	serializer.endTag("", NETWORKURI_TAG);
	        
	        	serializer.startTag("", NAME_TAG);
	        	serializer.text(getName());
	        	serializer.endTag("", NAME_TAG);
	        
	        	serializer.startTag("", WEBSITEURL_TAG);
	        	serializer.text(getWebsiteUrl());
	        	serializer.endTag("", WEBSITEURL_TAG);
			
	        	serializer.startTag("", HOTSPOTSCOUNT_TAG);
	        	serializer.text(""+getHotspotsCount());
	        	serializer.endTag("", HOTSPOTSCOUNT_TAG);
			
	        	serializer.startTag("", VALIDSUBSCRIBEDUSERSCOUNT_TAG);
	        	serializer.text(""+getValidSubscribedUsersCount());
	        	serializer.endTag("", VALIDSUBSCRIBEDUSERSCOUNT_TAG);
	        
	        serializer.endTag("", NETWORKMETADATA_TAG);
			
	        serializer.startTag("", HOTSPOTS_TAG);
	        for(int i=0;i<hotSpots.size();i++) {
		        hotSpots.get(i).writeXml(serializer);
	        }
	        serializer.endTag("", HOTSPOTS_TAG);
/*
              serializer.startTag("", "message");
	           serializer.attribute("", "date", msg.getDate());
        	   serializer.text(msg.getContent());
	           serializer.endTag("", "message");
*/
	        serializer.endTag("", "wifidogHotspotsStatus");
	        serializer.endDocument();
	        return ;
	    } catch (Exception e) {
	    	Log.i(CURRENTMODULE,"Error Writing Xml HotSpot Cache!");
	        e.printStackTrace();
	    } 
	}
	
	public String getStatusURL() {
		return statusURL;
	}

	public statusFormat getFormat() {
		return format;
	}

	public String getNetworkUri() {
		return networkUri;
	}

	public String getName() {
		return name;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public int getHotspotsCount() {
		return hotspotsCount;
	}

	public int getValidSubscribedUsersCount() {
		return validSubscribedUsersCount;
	}

	public int getOnlineUsersCount() {
		return onlineUsersCount;
	}

	public ArrayList<HotSpot> getHotSpots() {
		return hotSpots;
	}
	
	public void addHotSpot(HotSpot hs) {
		hotSpots.add(hs);
	}

	public void setStatusURL(String statusURL) {
		this.statusURL = statusURL;
	}

	public void setNetworkUri(String networkUri) {
		this.networkUri = networkUri;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public void setHotspotsCount(int hotspotsCount) {
		this.hotspotsCount = hotspotsCount;
	}

	public void setValidSubscribedUsersCount(int validSubscribedUsersCount) {
		this.validSubscribedUsersCount = validSubscribedUsersCount;
	}

	public void setOnlineUsersCount(int onlineUsersCount) {
		this.onlineUsersCount = onlineUsersCount;
	}

	public HotSpot findClosestHotSpot(double [] currentLocation) {
		double distance=1E6;
		HotSpot closestHS=null;
		for (HotSpot hs : hotSpots) {
			double newDistance=
				Math.sqrt(currentLocation[0]-hs.getGisCenterLatLong(0))+
				Math.sqrt(currentLocation[1]-hs.getGisCenterLatLong(1));
			if(newDistance < distance) {
				distance=newDistance ;
				closestHS=hs;
			}
		}
		return closestHS;
	}
}
