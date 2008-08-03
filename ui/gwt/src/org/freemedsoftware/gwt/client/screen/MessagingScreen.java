/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record and Practice Management System
 * Copyright (C) 1999-2008 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.freemedsoftware.gwt.client.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.freemedsoftware.gwt.client.ScreenInterface;
import org.freemedsoftware.gwt.client.Util;
import org.freemedsoftware.gwt.client.Api.ModuleInterfaceAsync;
import org.freemedsoftware.gwt.client.Module.MessagesModule;
import org.freemedsoftware.gwt.client.Module.MessagesModuleAsync;
import org.freemedsoftware.gwt.client.widget.ClosableTab;
import org.freemedsoftware.gwt.client.widget.CustomSortableTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MessagingScreen extends ScreenInterface {

	private CustomSortableTable wMessages;

	private HashMap<String, String>[] mStore;

	protected HTML messageView;

	public MessagingScreen() {
		final VerticalPanel verticalPanel = new VerticalPanel();
		initWidget(verticalPanel);

		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		verticalPanel.add(horizontalPanel);

		final Button composeButton = new Button();
		horizontalPanel.add(composeButton);
		composeButton.setText("Compose");
		composeButton.addClickListener(new ClickListener() {
			public void onClick(Widget w) {
				final MessagingComposeScreen p = new MessagingComposeScreen();
				p.assignState(state);
				state.getTabPanel().add(p,
						new ClosableTab("Compose Message", p));
				state.getTabPanel().selectTab(
						state.getTabPanel().getWidgetCount() - 1);
			}
		});

		final Button selectAllButton = new Button();
		horizontalPanel.add(selectAllButton);
		selectAllButton.setText("Select All");

		final Button selectNoneButton = new Button();
		horizontalPanel.add(selectNoneButton);
		selectNoneButton.setText("Select None");

		final VerticalPanel verticalSplitPanel = new VerticalPanel();
		verticalPanel.add(verticalSplitPanel);
		verticalSplitPanel.setSize("100%", "100%");
		// verticalSplitPanel.setSplitPosition("50%");

		wMessages = new CustomSortableTable();
		verticalSplitPanel.add(wMessages);
		wMessages.setSize("100%", "100%");
		wMessages.addColumn("Received", "stamp");
		wMessages.addColumn("From", "from_user");
		wMessages.addColumn("Subject", "subject");
		wMessages.setIndexName("id");
		wMessages.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents ste, int row, int col) {
				// Get information on row...
				try {
					final Integer messageId = new Integer(wMessages
							.getValueByRow(row));
					showMessage(messageId);
				} catch (Exception e) {
					GWT.log("Caught exception: ", e);
				}
			}
		});

		messageView = new HTML("Message text goes here.");
		verticalSplitPanel.add(messageView);
		messageView.setSize("100%", "100%");
		// verticalSplitPanel.setSize("100%", "100%");

		// Start population routine
		populate("");
	}

	public void populate(String tag) {
		if (Util.isStubbedMode()) {
			HashMap<String, String>[] dummyData = getStubData();
			populateByData(dummyData);
		} else {
			// Populate the whole thing.
			MessagesModuleAsync service = (MessagesModuleAsync) GWT
					.create(MessagesModule.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) service;
			String moduleRelativeURL = Util.getRelativeURL();
			endpoint.setServiceEntryPoint(moduleRelativeURL);
			service.GetAllByTag(tag, Boolean.FALSE,
					new AsyncCallback<HashMap<String, String>[]>() {
						public void onSuccess(HashMap<String, String>[] result) {
							populateByData(result);
						}

						public void onFailure(Throwable t) {
							GWT.log("Exception", t);
						}
					});
		}
	}

	/**
	 * Actual internal data population method, wrapped for testing.
	 * 
	 * @param data
	 */
	public void populateByData(HashMap<String, String>[] data) {
		// Keep a copy of the data in the local store
		mStore = data;
		// Clear any current contents
		wMessages.clear();
		wMessages.loadData(data);
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String>[] getStubData() {
		List<HashMap<String, String>> m = new ArrayList<HashMap<String, String>>();

		final HashMap<String, String> a = new HashMap<String, String>();
		a.put("id", "1");
		a.put("stamp", "2007-08-01");
		a.put("from_user", "A");
		a.put("subject", "Subject A");
		m.add(a);

		final HashMap<String, String> b = new HashMap<String, String>();
		b.put("id", "2");
		b.put("stamp", "2007-08-01");
		b.put("from_user", "B");
		b.put("subject", "Subject B");
		m.add(b);

		final HashMap<String, String> c = new HashMap<String, String>();
		c.put("id", "3");
		c.put("stamp", "2007-08-03");
		c.put("from_user", "C");
		c.put("subject", "Subject C");
		m.add(c);

		return (HashMap<String, String>[]) m.toArray(new HashMap<?, ?>[0]);
	}

	protected void showMessage(Integer messageId) {
		if (Util.isStubbedMode()) {
			String txt = new String();
			switch (messageId.intValue()) {
			case 1:
				txt = "Text from message A";
				break;
			case 2:
				txt = "Some more text from message B.";
				break;
			case 3:
				txt = "Why are you still clicking on me? I'm from message C.";
				break;
			default:
				txt = "";
				break;
			}
			messageView.setHTML(txt);
		} else {
			ModuleInterfaceAsync service = null;

			try {
				service = (ModuleInterfaceAsync) Util
						.getProxy("org.freemedsoftware.gwt.client.Api.ModuleInterface");
			} catch (Exception e) {
				GWT.log("Caught exception: ", e);
			}
			service.ModuleGetRecordMethod("MessagesModule", messageId,
					new AsyncCallback<HashMap<String, String>>() {
						public void onSuccess(HashMap<String, String> data) {
							messageView.setHTML(data.get("msgtext"));
						}

						public void onFailure(Throwable t) {
							GWT.log("Exception", t);
						}
					});
		}
	}
}
