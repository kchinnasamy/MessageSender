package com.kpj4s.communication;

import java.util.HashSet;
import java.util.Set;


public class ContactsParser {
	
	public Set<String> parse(String contactsString) {
		String[] contactStrings = contactsString.split(",");
		Set<String> contacts = new HashSet<String>();
		for (String contactString : contactStrings) {
			String number = parseContact(contactString);
			if (! number.equals("Empty")) {
				contacts.add(number);
			}
		}
		return contacts;
	}

	private String parseContact(String contactString) {
			int indexOfOpenAngleBracket = contactString.indexOf('<');
			if (indexOfOpenAngleBracket != -1) {
				int indexOfCloseAngleBracket = contactString.indexOf('>');
				contactString = contactString.substring(indexOfOpenAngleBracket + 1,
						indexOfCloseAngleBracket);
			}
            String phoneNumber = contactString.replaceAll("\\D+", "");
            if (phoneNumber.length() == 0) {
            	return "Empty";
            }
            int phoneNumberLength = phoneNumber.length();
            if (phoneNumberLength < 10) {
            	throw new IllegalArgumentException("Invalid phone number");
            }
            	return phoneNumber.substring(phoneNumberLength - 10,
            			phoneNumberLength);
         
	}
}
