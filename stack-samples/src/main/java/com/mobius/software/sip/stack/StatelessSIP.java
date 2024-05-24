package com.mobius.software.sip.stack;

import java.util.Properties;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class StatelessSIP implements SipListener {
	
	// This class demonstrates stateless SIP processing, where each request and response is
	// handled independently without maintaining any state information across multiple messages.
	
	 private static SipStack sipStack;
	    private static AddressFactory addressFactory;
	    private static MessageFactory messageFactory;
	    private static HeaderFactory headerFactory;
	    private static final String myAddress = "127.0.0.1";
	    private static final int myPort = 5080;
	    private SipProvider sipProvider;

	    public void init() {
	        try {
	            SipFactory sipFactory = SipFactory.getInstance();
	            sipFactory.setPathName("gov.nist");

	            Properties properties = new Properties();
	            properties.setProperty("javax.sip.STACK_NAME", "stateful");
	            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
	            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");

	            sipStack = sipFactory.createSipStack(properties);
	            addressFactory = sipFactory.createAddressFactory();
	            messageFactory = sipFactory.createMessageFactory();
	            headerFactory = sipFactory.createHeaderFactory();

	            ListeningPoint lp = sipStack.createListeningPoint(myAddress, myPort, ListeningPoint.UDP);
	            sipProvider = sipStack.createSipProvider(lp);
	            sipProvider.addSipListener(this);

	            sipStack.start();

	            // Here you can start and stop receiving messages
	            // Add shutdown hook
	            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

	        } catch (Exception e) {
	            e.printStackTrace();
	            System.exit(2);
	        }
	    }

	    public void stop() {
	    	//this method is the same for types all dialog
	        try {
	            if (sipProvider != null) {
	                sipProvider.removeSipListener(this);
	                sipStack.deleteSipProvider(sipProvider);
	                sipProvider = null;
	            }
	            if (sipStack != null) {
	                sipStack.stop();
	                sipStack = null;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    @Override
	    public void processRequest(RequestEvent requestEvent) {
	        Request request = requestEvent.getRequest();
	        ServerTransaction serverTransaction = requestEvent.getServerTransaction();

	        try {
	            if (request.getMethod().equals(Request.INVITE)) {
	                if (serverTransaction == null) {
	                    // In stateful processing, we create a new ServerTransaction if it doesn't exist
	                    serverTransaction = sipProvider.getNewServerTransaction(request);
	                }
	                processInvite(requestEvent, serverTransaction);
	            } else if (request.getMethod().equals(Request.BYE)) {
	                if (serverTransaction == null) {
	                    // Similarly, create a ServerTransaction for BYE requests
	                    serverTransaction = sipProvider.getNewServerTransaction(request);
	                }
	                processBye(requestEvent, serverTransaction);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    private void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
	        try {
	            Request request = requestEvent.getRequest();

	            // Statefully handle the INVITE request and send a RINGING response
	            Response ringingResponse = messageFactory.createResponse(Response.RINGING, request);
	            ToHeader toHeader = (ToHeader) ringingResponse.getHeader(ToHeader.NAME);
	            toHeader.setTag("12345"); // This is mandatory as per the spec.
	            serverTransaction.sendResponse(ringingResponse);

	            // Send OK response
	            Response okResponse = messageFactory.createResponse(Response.OK, request);
	            Address address = addressFactory.createAddress("StatefulSIP <sip:" + myAddress + ":" + myPort + ">");
	            ContactHeader contactHeader = headerFactory.createContactHeader(address);
	            okResponse.addHeader(contactHeader);
	            serverTransaction.sendResponse(okResponse);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    private void processBye(RequestEvent requestEvent, ServerTransaction serverTransaction) {
	        try {
	            Request request = requestEvent.getRequest();
	            // Statefully handle the BYE request and send a 200 OK response
	            Response response = messageFactory.createResponse(200, request);
	            serverTransaction.sendResponse(response);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
    @Override
    public void processResponse(ResponseEvent responseEvent) {
        // Process SIP responses here
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        // Handle timeout events here
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        // Handle IO exceptions here
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        // Handle transaction terminated events here
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        // Handle dialog terminated events here
    }

    // Here you can add additional listeners and/or sip providers accordingly to your app
}


