package com.mobius.software.sip.stack;

import java.util.Properties;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class DialogBasedSIP implements SipListener {

    private static SipStack sipStack;
    private static AddressFactory addressFactory;
    private static MessageFactory messageFactory;
    private static HeaderFactory headerFactory;
    private static final String myAddress = "127.0.0.1";
    private static final int myPort = 5080;
    private SipProvider sipProvider;
    private Dialog dialog;

    public void init() {
        try {
            SipFactory sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");

            Properties properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", "dialogBased");
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "on");

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
        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent);
        }
    }

    private void processInvite(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();

            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(request);
            }

            Response ringingResponse = messageFactory.createResponse(Response.RINGING, request);
            ToHeader toHeader = (ToHeader) ringingResponse.getHeader(ToHeader.NAME);
            toHeader.setTag("12345"); // This is mandatory as per the spec.
            serverTransaction.sendResponse(ringingResponse);

            Response okResponse = messageFactory.createResponse(Response.OK, request);
            Address address = addressFactory.createAddress("DialogBasedSIP <sip:" + myAddress + ":" + myPort + ">");
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            okResponse.addHeader(contactHeader);
            serverTransaction.sendResponse(okResponse);

            dialog = serverTransaction.getDialog();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAck(RequestEvent requestEvent) {
        dialog = requestEvent.getDialog();
        sendBye();
    }

    private void processBye(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            Response response = messageFactory.createResponse(200, request);
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();

            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(request);
            }

            serverTransaction.sendResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        int status = response.getStatusCode();
        if (status == Response.OK && ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod().equals(Request.INVITE)) {
            Dialog responseDialog = responseEvent.getDialog();
            if (responseDialog != null) {
                this.dialog = responseDialog;
                sendAck(responseEvent); 
            } else {
                System.err.println("Error: Received 200 OK without a valid dialog.");
            }
        }
    }

    private void sendAck(ResponseEvent responseEvent) {
        if (this.dialog != null) {
            try {
                Response response = responseEvent.getResponse();
                CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                dialog.sendAck(ackRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Dialog is null, cannot send ACK request.");
        }
    }

    private void sendBye() {
        try {
            if (dialog != null) {
                Request byeRequest = dialog.createRequest(Request.BYE);
                ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
                dialog.sendRequest(ct);
            } else {
                System.err.println("Dialog is null, cannot send BYE request.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

