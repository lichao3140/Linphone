/*
LinphoneContact.java
Copyright (C) 2016  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.linphone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriend.SubscribePolicy;
import org.linphone.core.PresenceBasicStatus;
import org.linphone.mediastream.Log;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;

import org.linphone.R;

public class LinphoneContact implements Serializable, Comparable<LinphoneContact> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9015568163905205244L;

	private transient LinphoneFriend friend;
	private String fullName, firstName, lastName, androidId, androidRawId, androidTagId;
	private transient Uri photoUri, thumbnailUri;
	private List<LinphoneNumberOrAddress> addresses;
	private transient ArrayList<ContentProviderOperation> changesToCommit;
	private transient ArrayList<ContentProviderOperation> changesToCommit2;
	private boolean hasSipAddress;
	
	public LinphoneContact() {
		addresses = new ArrayList<LinphoneNumberOrAddress>();
		androidId = null;
		thumbnailUri = null;
		photoUri = null;
		changesToCommit = new ArrayList<ContentProviderOperation>();
		changesToCommit2 = new ArrayList<ContentProviderOperation>();
		hasSipAddress = false;
	}

	public void setFullName(String name) {
		fullName = name;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFirstNameAndLastName(String fn, String ln) {
		if (fn != null && fn.length() == 0 && ln != null && ln.length() == 0) return;
		
		if (isAndroidContact()) {
			if (firstName != null || lastName != null) {
				String select = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'";
				String[] args = new String[]{ getAndroidId() };
	
				changesToCommit.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
					.withSelection(select, args)
					.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, fn)
					.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, ln)
					.build()
				);
			} else {
				changesToCommit.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			        .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, fn)
			        .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, ln)
			        .build());
			}
		}
		
		firstName = fn;
		lastName = ln;
		if (firstName != null && lastName != null && firstName.length() > 0 && lastName.length() > 0) {
			fullName = firstName + " " + lastName;
		} else if (firstName != null && firstName.length() > 0) {
			fullName = firstName;
		} else if (lastName != null && lastName.length() > 0) {
			fullName = lastName;
		}
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public boolean hasPhoto() {
		return photoUri != null;
	}
	
	public void setPhotoUri(Uri uri) {
		photoUri = uri;
	}
	
	public Uri getPhotoUri() {
		return photoUri;
	}
	
	public void setThumbnailUri(Uri uri) {
		thumbnailUri = uri;
	}

	public Uri getThumbnailUri() {
		return thumbnailUri;
	}
	
	public void setPhoto(byte[] photo) {
		if (photo != null) {
			if (isAndroidContact()) {
				if (androidRawId != null) {
					changesToCommit.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValue(ContactsContract.Data.RAW_CONTACT_ID, androidRawId)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photo)
						.withValue(ContactsContract.Data.IS_PRIMARY, 1)
						.withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
						.build());
				} else {
					changesToCommit.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photo)
						.build());
				}
			}
		}
	}

	public void addNumberOrAddress(LinphoneNumberOrAddress noa) {
		if (noa == null) return;
		if (noa.isSIPAddress()) {
			hasSipAddress = true;
		}
		addresses.add(noa);
	}

	public List<LinphoneNumberOrAddress> getNumbersOrAddresses() {
		return addresses;
	}

	public boolean hasAddress(String address) {
		for (LinphoneNumberOrAddress noa : getNumbersOrAddresses()) {
			if (noa.isSIPAddress()) {
				String value = noa.getValue();
				if (value.equals(address) || value.equals("sip:" + address)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasAddress() {
		return hasSipAddress;
	}
	
	public void removeNumberOrAddress(LinphoneNumberOrAddress noa) {
		if (noa != null && noa.getOldValue() != null) {
			if (isAndroidContact()) {
				String select;
				if (noa.isSIPAddress()) {
					select = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS + "=?";
				} else {
					select = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + "=?";
				}
				String[] args = new String[]{ getAndroidId(), noa.getOldValue() };
		
				changesToCommit.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
					.withSelection(select, args)
					.build());
				
				if (androidTagId != null && noa.isSIPAddress()) {
					select = ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.DATA1 + "=?";
					args = new String[] { androidTagId, noa.getOldValue() };

					changesToCommit.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
						.withSelection(select, args)
						.build());
				}
			}
			
			if (isLinphoneFriend()) {
				if (noa.isSIPAddress()) {
					if (!noa.getOldValue().startsWith("sip:")) {
						noa.setOldValue("sip:" + noa.getOldValue());
					}
				}
				LinphoneNumberOrAddress toRemove = null;
				for (LinphoneNumberOrAddress address : addresses) {
					if (noa.getOldValue().equals(address.getValue()) && noa.isSIPAddress() == address.isSIPAddress()) {
						toRemove = address;
						break;
					}
				}
				if (toRemove != null) {
					addresses.remove(toRemove);
				}
			}
		}
	}
	
	public void addOrUpdateNumberOrAddress(LinphoneNumberOrAddress noa) {
		if (noa != null && noa.getValue() != null) {
			if (isAndroidContact()) {
				if (noa.getOldValue() == null) {
					ContentValues values = new ContentValues();
					if (noa.isSIPAddress()) {
						values.put(ContactsContract.Data.MIMETYPE, CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE);
				        values.put(ContactsContract.CommonDataKinds.SipAddress.DATA, noa.getValue());
						values.put(CommonDataKinds.SipAddress.TYPE, CommonDataKinds.SipAddress.TYPE_CUSTOM);
						values.put(CommonDataKinds.SipAddress.LABEL, ContactsManager.getInstance().getString(R.string.addressbook_label));
					} else {
				        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, noa.getValue());
				        values.put(ContactsContract.CommonDataKinds.Phone.TYPE,  ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
						values.put(ContactsContract.CommonDataKinds.Phone.LABEL, ContactsManager.getInstance().getString(R.string.addressbook_label));
					}
					if (androidRawId != null) {
						changesToCommit.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValue(ContactsContract.Data.RAW_CONTACT_ID, androidRawId)
					        .withValues(values)
					        .build());
					} else {
						changesToCommit.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
					        .withValues(values)
					        .build());
					}
					
					if (noa.isSIPAddress() && LinphoneManager.getInstance().getContext().getResources().getBoolean(R.bool.use_linphone_tag)) {
						if (androidTagId != null) {
							changesToCommit.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValue(ContactsContract.Data.RAW_CONTACT_ID, androidTagId)
								.withValue(ContactsContract.Data.MIMETYPE, ContactsManager.getInstance().getString(R.string.sync_mimetype))
								.withValue(ContactsContract.Data.DATA1, noa.getValue())
								.withValue(ContactsContract.Data.DATA2, ContactsManager.getInstance().getString(R.string.app_name))
								.withValue(ContactsContract.Data.DATA3, noa.getValue())
								.build());
						} else {
							changesToCommit2.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(ContactsContract.Data.MIMETYPE, ContactsManager.getInstance().getString(R.string.sync_mimetype))
								.withValue(ContactsContract.Data.DATA1, noa.getValue())
								.withValue(ContactsContract.Data.DATA2, ContactsManager.getInstance().getString(R.string.app_name))
								.withValue(ContactsContract.Data.DATA3, noa.getValue())
								.build());
						}
					}
				} else {
					ContentValues values = new ContentValues();
					String select;
					String[] args = new String[] { getAndroidId(), noa.getOldValue() };
					
					if (noa.isSIPAddress()) {
						select = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS + "=?";
						values.put(ContactsContract.Data.MIMETYPE, CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE);
				        values.put(ContactsContract.CommonDataKinds.SipAddress.DATA, noa.getValue());
					} else {
						select = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE +  "' AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + "=?"; 
				        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, noa.getValue());
					}
					changesToCommit.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
						.withSelection(select, args)
				        .withValues(values)
				        .build());
					
					if (noa.isSIPAddress() && LinphoneManager.getInstance().getContext().getResources().getBoolean(R.bool.use_linphone_tag)) {
						if (androidTagId != null) {
							changesToCommit.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
								.withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.DATA1 + "=? ", new String[] { androidTagId, noa.getOldValue() })
								.withValue(ContactsContract.Data.DATA1, noa.getValue())
								.withValue(ContactsContract.Data.DATA2, ContactsManager.getInstance().getString(R.string.app_name))
								.withValue(ContactsContract.Data.DATA3, noa.getValue())
								.build());
						} else {
							changesToCommit2.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(ContactsContract.Data.MIMETYPE, ContactsManager.getInstance().getString(R.string.sync_mimetype))
								.withValue(ContactsContract.Data.DATA1, noa.getValue())
								.withValue(ContactsContract.Data.DATA2, ContactsManager.getInstance().getString(R.string.app_name))
								.withValue(ContactsContract.Data.DATA3, noa.getValue())
								.build());
						}
					}
				}
			}
			if (isLinphoneFriend()) {
				if (noa.isSIPAddress()) {
					if (!noa.getValue().startsWith("sip:")) {
						noa.setValue("sip:" + noa.getValue());
					}
				}
				if (noa.getOldValue() != null) {
					if (noa.isSIPAddress()) {
						if (!noa.getOldValue().startsWith("sip:")) {
							noa.setOldValue("sip:" + noa.getOldValue());
						}
					}
					for (LinphoneNumberOrAddress address : addresses) {
						if (noa.getOldValue().equals(address.getValue()) && noa.isSIPAddress() == address.isSIPAddress()) {
							address.setValue(noa.getValue());
							break;
						}
					}
				} else {
					addresses.add(noa);
				}
			}
		}
	}
	
	public void setAndroidId(String id) {
		androidId = id;
	}

	public String getAndroidId() {
		return androidId;
	}
	
	public void save() {		
		if (isAndroidContact() && ContactsManager.getInstance().hasContactsAccess() && changesToCommit.size() > 0) {		
			try {
				ContactsManager.getInstance().getContentResolver().applyBatch(ContactsContract.AUTHORITY, changesToCommit);
				createLinphoneTagIfNeeded();
			} catch (Exception e) {
				Log.e(e);
			} finally {
				changesToCommit = new ArrayList<ContentProviderOperation>();
				changesToCommit2 = new ArrayList<ContentProviderOperation>();
			}
		}
		
		if (isLinphoneFriend()) {
			boolean hasAddr = false;
			LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
			if (lc == null) return;
			
			friend.edit();
			for (LinphoneAddress address : friend.getAddresses()) {
				friend.removeAddress(address);
			}
			for (String phone : friend.getPhoneNumbers()) {
				friend.removePhoneNumber(phone);
			}
			for (LinphoneNumberOrAddress noa : addresses) {
				if (noa.isSIPAddress()) {
					try {
						LinphoneAddress addr = lc.interpretUrl(noa.getValue());
						if (addr != null) {
							friend.addAddress(addr);
							hasAddr = true;
						}
					} catch (LinphoneCoreException e) {
						Log.e(e);
					}
				} else {
					friend.addPhoneNumber(noa.getValue());
				}
			}
			if (hasAddr) {
				friend.setName(fullName);
			}
			friend.done();
			
			if (friend.getAddress() != null) {
				if (lc.findFriendByAddress(friend.getAddress().asString()) == null) {
					try {
						lc.addFriend(friend);
						ContactsManager.getInstance().fetchContacts();
					} catch (LinphoneCoreException e) {
						Log.e(e);
					}
				}
			}
		}
	}

	public void delete() {
		if (isAndroidContact()) {
			String select = ContactsContract.Data.CONTACT_ID + " = ?";
			String[] args = new String[] { getAndroidId() };
			changesToCommit.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).withSelection(select, args).build());
			save();
		}
		if (isLinphoneFriend()) {
			deleteFriend();
		}
	}
	
	public void deleteFriend() {
		if (friend != null) {
			LinphoneManager.getLcIfManagerNotDestroyedOrNull().removeFriend(friend);
		}
	}

	public void refresh() {
		addresses = new ArrayList<LinphoneNumberOrAddress>();
		hasSipAddress = false;
		
		if (isAndroidContact()) {
			getContactNames();
			setThumbnailUri(getContactPictureUri());
			setPhotoUri(getContactPhotoUri());
			androidRawId = findRawContactID();
			
			if (LinphoneManager.getInstance().getContext().getResources().getBoolean(R.bool.use_linphone_tag)) {
				androidTagId = findLinphoneRawContactId();
			}
			
			for (LinphoneNumberOrAddress noa : getAddressesAndNumbersForAndroidContact()) {
				addNumberOrAddress(noa);
			}
			
			if (friend == null) {
				friend = LinphoneCoreFactory.instance().createLinphoneFriend();
				friend.setRefKey(getAndroidId());
				// Disable subscribes for now
				friend.enableSubscribes(false);
				friend.setIncSubscribePolicy(SubscribePolicy.SPDeny);
				if (hasSipAddress) {
					for (LinphoneNumberOrAddress noa : addresses) {
						if (noa.isSIPAddress()) {
							try {
								LinphoneAddress addr = LinphoneManager.getLc().interpretUrl(noa.getValue());
								if (addr != null) {
									friend.addAddress(addr);
								}
							} catch (LinphoneCoreException e) {
								Log.e(e);
							}
						} else {
							friend.addPhoneNumber(noa.getValue());
						}
					}
				}
				LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
				if (lc != null && friend.getAddress() != null) {
					friend.setName(fullName);
					try {
						lc.addFriend(friend);
					} catch (LinphoneCoreException e) {
						Log.e(e);
					}
				}
			}
		} else if (isLinphoneFriend()) {
			fullName = friend.getName();
			thumbnailUri = null;
			photoUri = null;
			LinphoneAddress addr = friend.getAddress();
			if (addr != null) {
				addresses.add(new LinphoneNumberOrAddress(addr.asStringUriOnly(), true));
				hasSipAddress = true;
			}
		}
	}
	
	public boolean isAndroidContact() {
		return androidId != null;
	}
	
	public boolean isLinphoneFriend() {
		return friend != null;
	}

	public boolean isInLinphoneFriendList() {
		return (friend != null && friend.getPresenceModel() != null &&  friend.getPresenceModel().getBasicStatus().equals(PresenceBasicStatus.Open));
	}

	public void setFriend(LinphoneFriend f) {
		friend = f;
	}

	public static LinphoneContact createContact() {
		if (ContactsManager.getInstance().hasContactsAccess()) {
			return createAndroidContact();
		}
		return createLinphoneFriend();
	}
	
	@Override
	public int compareTo(LinphoneContact contact) {
		String fullName = getFullName();
		String contactFullName = contact.getFullName();
		String firstLetter = fullName == null || fullName.isEmpty() ? "" : fullName.substring(0, 1).toUpperCase(Locale.getDefault());
		String contactfirstLetter = contactFullName == null || contactFullName.isEmpty() ? "" : contactFullName.substring(0, 1).toUpperCase(Locale.getDefault());
		return firstLetter.compareTo(contactfirstLetter);
	}

	private Uri getContactPictureUri() {
		Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(getAndroidId()));
		return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}

	private Uri getContactPhotoUri() {
		Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(getAndroidId()));
		return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
	}
	
	private void getContactNames() {
		ContentResolver resolver = ContactsManager.getInstance().getContentResolver();
		String[] proj = new String[]{ ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, ContactsContract.Contacts.DISPLAY_NAME };
		String select = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?";
		String[] args = new String[]{ getAndroidId(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
		Cursor c = resolver.query(ContactsContract.Data.CONTENT_URI, proj, select, args, null);
		if (c != null) {
			if (c.moveToFirst()) {
				firstName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
				lastName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
	        	fullName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			}
			c.close();
		}
	}
	
	private String findRawContactID() {
		ContentResolver resolver = ContactsManager.getInstance().getContentResolver();
		String result = null;
		String[] projection = { ContactsContract.RawContacts._ID };
		
		String selection = ContactsContract.RawContacts.CONTACT_ID + "=?";
		Cursor c = resolver.query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, new String[]{ getAndroidId() }, null);
		if (c != null) {
			if (c.moveToFirst()) {
				result = c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID));
			}
			c.close();
		}
		return result;
	}
	
	private List<LinphoneNumberOrAddress> getAddressesAndNumbersForAndroidContact() {
		List<LinphoneNumberOrAddress> result = new ArrayList<LinphoneNumberOrAddress>();
		ContentResolver resolver = ContactsManager.getInstance().getContentResolver();
		
		String select = ContactsContract.Data.CONTACT_ID + " =? AND (" + ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?)";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS, ContactsContract.Data.MIMETYPE }; // PHONE_NUMBER == SIP_ADDRESS == "data1"...
		Cursor c = resolver.query(ContactsContract.Data.CONTENT_URI, projection, select, new String[]{ getAndroidId(), ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE }, null);
		if (c != null) {
			while (c.moveToNext()) {
				String mime = c.getString(c.getColumnIndex(ContactsContract.Data.MIMETYPE));
				if (mime != null && mime.length() > 0) {
					boolean found = false;
					boolean isSIP = false;
					if (mime.equals(ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)) {
						found = true;
						isSIP = true;
					} else if (mime.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
						found = true;
					}
					
					if (found) {
						String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS)); // PHONE_NUMBER == SIP_ADDRESS == "data1"...
						if (number != null && number.length() > 0) {
							if (isSIP && !number.startsWith("sip:")) {
								number = "sip:" + number;
							}
							result.add(new LinphoneNumberOrAddress(number, isSIP));
						}
					}
				}
			}
			c.close();
		}
		
		return result;
	}

	private static LinphoneContact createAndroidContact() {
		LinphoneContact contact = new LinphoneContact();
		
		contact.changesToCommit.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
	        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
	        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
	        .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
	        .build());
		contact.setAndroidId("0");
		
		return contact;
	}

	private static LinphoneContact createLinphoneFriend() {
		LinphoneContact contact = new LinphoneContact();
		LinphoneFriend friend = LinphoneCoreFactory.instance().createLinphoneFriend();
		// Disable subscribes for now
		friend.enableSubscribes(false);
		friend.setIncSubscribePolicy(SubscribePolicy.SPDeny);
		contact.friend = friend;
		return contact;
	}
	
	private String findLinphoneRawContactId() {
		ContentResolver resolver = ContactsManager.getInstance().getContentResolver();
		String result = null;
		String[] projection = { ContactsContract.RawContacts._ID };
		
		String selection = ContactsContract.RawContacts.CONTACT_ID + "=? AND " + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
		Cursor c = resolver.query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, new String[] { getAndroidId(), ContactsManager.getInstance().getString(R.string.sync_account_type) }, null);
		if (c != null) {
			if (c.moveToFirst()) {
				result = c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID));
			}
			c.close();
		}
		return result;
	}
	
	private void createLinphoneTagIfNeeded() {
		if (LinphoneManager.getInstance().getContext().getResources().getBoolean(R.bool.use_linphone_tag)) {
			if (androidTagId == null && findLinphoneRawContactId() == null) {
				createLinphoneContactTag();
			}
		}
	}
	
	private void createLinphoneContactTag() {
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		batch.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
			.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, ContactsManager.getInstance().getString(R.string.sync_account_type))
			.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, ContactsManager.getInstance().getString(R.string.sync_account_name))
			.withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
			.build());

		batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, getFullName())
			.build());

		batch.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
			.withValue(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
			.withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, androidRawId)
			.withValueBackReference(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, 0)
			.build());
		
		if (changesToCommit2.size() > 0) {
			for(ContentProviderOperation cpo : changesToCommit2) {
				batch.add(cpo);
			}
		}
		
		try {
			ContactsManager.getInstance().getContentResolver().applyBatch(ContactsContract.AUTHORITY, batch);
			androidTagId = findLinphoneRawContactId();
		} catch (Exception e) {
			Log.e(e);
		}
	}
}
