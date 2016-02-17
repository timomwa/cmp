package com.pixelandtag.util;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class Concatenator {

	private static Logger LOGGER = Logger.getLogger(Concatenator.class);

    /**
     * @param args
     */
    public static final int MAX_SMS_LENGTH = 160;
    private final static int MAX_MESSAGE_SEGMENT_8BIT = 133; // 140-7
    private final static int MAX_MESSAGE_SEGMENT_7BIT = 152;
    private final static byte UDHIE_IDENTIFIER_SAR = 0x08;
    private final static byte UDHIE_SAR_LENGTH = 0x04;
    private static int referenceNumber = 0;
    private static int referenceNumberII = 0;
    public static final int MAX_SMS_PAYLOAD_LENGTH = 440;

    private synchronized int getReferenceNumber() {
        referenceNumber++;
        if (referenceNumber > 65536) {
            referenceNumber = 0;
        }
        return referenceNumber;
    }

    private synchronized int getReferenceNumberII() {
        referenceNumberII++;
        if (referenceNumberII > 255) {
            referenceNumberII = 0;
        }
        return referenceNumberII;
    }

    public static Vector<String> splitTextForPayload(String input) {
        Vector<String> ret = new Vector<String>();
        if (input.trim().length() <= MAX_SMS_PAYLOAD_LENGTH) {
            ret.add(input.trim());
            return ret;
        }
        while (true) {
            input = input.trim();
            if (input.length() <= 436) {
                ret.add(input);
                break;
            }
            int pos = 436;

            while (input.charAt(pos) != ' ' && input.charAt(pos) != '\n'
                    && pos > 0) {
                pos--;
            }
            if (pos == 0) {
                pos = 436;
            }
            String tmp = input.substring(0, pos);
            ret.add(tmp);
            input = input.substring(pos);
        }

        return ret;
    }

    public byte[][] splitMessage7Bit(byte[] aMessage) {
        // determine how many messages
        int segmentNum = aMessage.length / MAX_MESSAGE_SEGMENT_7BIT;
        int messageLength = aMessage.length;
        if (segmentNum > 255) {
            // this is too long, can't fit, so chop
            segmentNum = 255;
            messageLength = segmentNum * MAX_MESSAGE_SEGMENT_7BIT;
        }
        if ((messageLength % MAX_MESSAGE_SEGMENT_7BIT) > 0) {
            segmentNum++;
        }

        byte[][] segments = new byte[segmentNum][];

        int lengthOfData;
        byte[] data7Bit;
        byte[] tempBytes = new byte[MAX_MESSAGE_SEGMENT_7BIT];
        byte[] refNum = copyShort2Bytes(getReferenceNumber());
        for (int i = 0; i < segmentNum; i++) {
            if (segmentNum - i == 1) {
                lengthOfData = messageLength - i * MAX_MESSAGE_SEGMENT_7BIT;
            } else {
                lengthOfData = MAX_MESSAGE_SEGMENT_7BIT;
            }
            System.arraycopy(aMessage, i * MAX_MESSAGE_SEGMENT_7BIT, tempBytes,
                    0, lengthOfData);
            data7Bit = encode7Bit(new String(tempBytes, 0, lengthOfData));
            segments[i] = new byte[7 + data7Bit.length];

            segments[i][0] = 6; // doesn't include itself
            // SAR identifier
            segments[i][1] = UDHIE_IDENTIFIER_SAR;
            // SAR length
            segments[i][2] = UDHIE_SAR_LENGTH;
            // DATAGRAM REFERENCE NUMBER
            System.arraycopy(refNum, 0, segments[i], 3, 2);
            // total number of segments
            segments[i][5] = (byte) segmentNum;
            // segment #
            segments[i][6] = (byte) (i + 1);
            // now copy the data
            System.arraycopy(data7Bit, 0, segments[i], 7, data7Bit.length);
        }

        return segments;
    }

    private byte[] encode7Bit(String aString) {
        int i, j, power;
        int length = aString.length();
        char[] tempChars = new char[length + 1];
        byte[] tempBytes = new byte[length];

        aString.getChars(0, length, tempChars, 0);
        tempChars[length] = 0;

        for (i = 0, j = 0, power = 1; i < length; i++, j++, power++) {
            if (power == 8) {
                i++;
                if (i >= length) {
                    break;
                }
                power = 1;
            }
            tempBytes[j] = (byte) ((tempChars[i] & ((1 << (8 - power)) - 1)) | ((tempChars[i + 1] & ((1 << power) - 1)) << (8 - power)));
            tempChars[i + 1] = (char) (tempChars[i + 1] >> power);
        }

        byte[] bytes = new byte[j];
        System.arraycopy(tempBytes, 0, bytes, 0, j);
        return bytes;
    }

    public byte[][] splitMessage8Bit(byte[] aMessage) {
        // determine how many messages
        int segmentNum = aMessage.length / MAX_MESSAGE_SEGMENT_8BIT;
        int messageLength = aMessage.length;
        if (segmentNum > 255) {
            // this is too long, can't fit, so chop
            segmentNum = 255;
            messageLength = segmentNum * MAX_MESSAGE_SEGMENT_8BIT;
        }
        if ((messageLength % MAX_MESSAGE_SEGMENT_8BIT) > 0) {
            segmentNum++;
        }
        byte[][] segments = new byte[segmentNum][];

        int lengthOfData;
        byte[] refNumber = copyShort2Bytes(getReferenceNumber());
        for (int i = 0; i < segmentNum; i++) {
            if (segmentNum - i == 1) {
                lengthOfData = messageLength - i * MAX_MESSAGE_SEGMENT_8BIT;
            } else {
                lengthOfData = MAX_MESSAGE_SEGMENT_8BIT;
            }
            segments[i] = new byte[7 + lengthOfData];
            segments[i][0] = 6; // doesn't include itself, is header length
            // SAR identifier
            segments[i][1] = UDHIE_IDENTIFIER_SAR;
            // SAR length
            segments[i][2] = UDHIE_SAR_LENGTH;
            // DATAGRAM REFERENCE NUMBER
            System.arraycopy(refNumber, 0, segments[i], 3, 2);
            // total number of segments
            segments[i][5] = (byte) segmentNum;
            // segment #
            segments[i][6] = (byte) (i + 1);
            LOGGER.info("Ref " + (i + 1));
            // now copy the data
            System.arraycopy(aMessage, i * MAX_MESSAGE_SEGMENT_8BIT,
                    segments[i], 7, lengthOfData);
        }

        return segments;

    }

    public byte[][] splitMessage8BitII(byte[] aMessage) {
        // determine how many messages
        int segmentNum = aMessage.length / MAX_MESSAGE_SEGMENT_8BIT;
        int messageLength = aMessage.length;
        if (segmentNum > 255) {
            // this is too long, can't fit, so chop
            segmentNum = 255;
            messageLength = segmentNum * MAX_MESSAGE_SEGMENT_8BIT;
        }
        if ((messageLength % MAX_MESSAGE_SEGMENT_8BIT) > 0) {
            segmentNum++;
        }
        byte[][] segments = new byte[segmentNum][];

        int lengthOfData;
        byte[] refNumber = copyShort2Bytes(getReferenceNumberII());
        for (int i = 0; i < segmentNum; i++) {
            if (segmentNum - i == 1) {
                lengthOfData = messageLength - i * MAX_MESSAGE_SEGMENT_8BIT;
            } else {
                lengthOfData = MAX_MESSAGE_SEGMENT_8BIT;
            }
            segments[i] = new byte[7 + lengthOfData];
            segments[i][0] = 6; // doesn't include itself, is header length
            // SAR identifier
            segments[i][1] = UDHIE_IDENTIFIER_SAR;
            // SAR length
            segments[i][2] = UDHIE_SAR_LENGTH;
            // DATAGRAM REFERENCE NUMBER
            System.arraycopy(refNumber, 0, segments[i], 3, 2);
            // total number of segments
            segments[i][5] = (byte) segmentNum;
            // segment #
            segments[i][6] = (byte) (i + 1);
            // now copy the data
            System.arraycopy(aMessage, i * MAX_MESSAGE_SEGMENT_8BIT,
                    segments[i], 7, lengthOfData);
        }

        return segments;

    }

    public byte[][] splitMessage8Bit3(byte[] aMessage) {
        // determine how many messages
        int segmentNum = aMessage.length / MAX_MESSAGE_SEGMENT_8BIT;
        int messageLength = aMessage.length;
        if (segmentNum > 255) {
            // this is too long, can't fit, so chop
            segmentNum = 255;
            messageLength = segmentNum * MAX_MESSAGE_SEGMENT_8BIT;
        }
        if ((messageLength % MAX_MESSAGE_SEGMENT_8BIT) > 0) {
            segmentNum++;
        }
        byte[][] segments = new byte[segmentNum][];

        int lengthOfData;
        byte[] refNumber = copyShort2Bytes(getReferenceNumber());
        for (int i = 0; i < segmentNum; i++) {
            if (segmentNum - i == 1) {
                lengthOfData = messageLength - (i * MAX_MESSAGE_SEGMENT_8BIT);
            } else {
                lengthOfData = MAX_MESSAGE_SEGMENT_8BIT;
            }
            segments[i] = new byte[7 + lengthOfData];
            segments[i][0] = 6; // doesn't include itself, is header length
            // SAR identifier
            segments[i][1] = UDHIE_IDENTIFIER_SAR;
            // SAR length
            segments[i][2] = UDHIE_SAR_LENGTH;
            // DATAGRAM REFERENCE NUMBER
            System.arraycopy(refNumber, 0, segments[i], 3, 2);
            // total number of segments
            segments[i][5] = (byte) segmentNum;
            // segment #
            segments[i][6] = (byte) (i + 1);
            // now copy the data
            System.arraycopy(aMessage, i * MAX_MESSAGE_SEGMENT_8BIT,
                    segments[i], 7, lengthOfData);
        }

        return segments;

    }

    private byte[] copyShort2Bytes(int integer) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((integer >> 8) & 0x0000ff);
        bytes[1] = (byte) (integer & 0x000000ff);
        return bytes;
    }

    public Concatenator() {

    }

    public void test() {
        // getConcatenated("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");

    }

    public static void main(String[] args) {
        new Concatenator().test();
    }

}
