/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record and Practice Management System
 * Copyright (C) 1999-2012 FreeMED Software Foundation
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

import static org.freemedsoftware.gwt.client.i18n.I18nUtil._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.freemedsoftware.gwt.client.CurrentState;
import org.freemedsoftware.gwt.client.JsonUtil;
import org.freemedsoftware.gwt.client.ScreenInterface;
import org.freemedsoftware.gwt.client.SystemEvent;
import org.freemedsoftware.gwt.client.Util;
import org.freemedsoftware.gwt.client.Util.ProgramMode;
import org.freemedsoftware.gwt.client.Api.ModuleInterfaceAsync;
import org.freemedsoftware.gwt.client.i18n.AppConstants;
import org.freemedsoftware.gwt.client.screen.patient.VitalsEntry;
import org.freemedsoftware.gwt.client.widget.CustomButton;
import org.freemedsoftware.gwt.client.widget.CustomTable;
import org.freemedsoftware.gwt.client.widget.CustomTable.TableRowClickHandler;
import org.freemedsoftware.gwt.client.widget.PatientWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TriageScreen extends ScreenInterface implements
		SystemEvent.Handler {

	protected CustomTable wTriagePendingList = null;

	protected TextBox wNote = null;

	protected PatientWidget wExistingPatient = null;

	protected Integer currentId = new Integer(0);

	protected Label showPatientName = null;

	protected FlexTable flexTable;

	protected HashMap<String, String>[] store = null;

	private static List<TriageScreen> triageScreenList = null;

	protected HorizontalPanel mainHorizontalPanel;

	// Creates only desired amount of instances if we follow this pattern
	// otherwise we have public constructor as well

	public static TriageScreen getInstance() {
		TriageScreen triageScreen = null;

		if (triageScreenList == null)
			triageScreenList = new ArrayList<TriageScreen>();
		if (triageScreenList.size() < AppConstants.MAX_TRIAGE_TABS)
			triageScreenList.add(triageScreen = new TriageScreen());
		else
			// returns last instance of TriageScreen from list
			triageScreen = triageScreenList
					.get(AppConstants.MAX_TRIAGE_TABS - 1);
		return triageScreen;
	}

	public static boolean removeInstance(TriageScreen triageScreen) {
		return triageScreenList.remove(triageScreen);
	}

	public TriageScreen() {
		super();
		VerticalPanel parentVp = new VerticalPanel();
		parentVp.setSize("10o%", "100%");
		initWidget(parentVp);
		mainHorizontalPanel = new HorizontalPanel();
		parentVp.add(mainHorizontalPanel);

		mainHorizontalPanel.setSize("100%", "100%");

		final VerticalPanel verticalPanel = new VerticalPanel();
		mainHorizontalPanel.add(verticalPanel);
		verticalPanel.setSize("100%", "100%");

		wTriagePendingList = new CustomTable();
		verticalPanel.add(wTriagePendingList);
		wTriagePendingList.setIndexName("id");

		// Form columns
		wTriagePendingList.addColumn(_("Registration Time"), "dateof");
		wTriagePendingList.addColumn(_("Last Name"), "lastname");
		wTriagePendingList.addColumn(_("First Name"), "firstname");
		wTriagePendingList.addColumn(_("DOB"), "dob");
		wTriagePendingList.addColumn(_("Gender"), "gender");
		wTriagePendingList.addColumn(_("Age"), "age");
		wTriagePendingList.addColumn(_("Notes"), "notes");

		wTriagePendingList.setTableRowClickHandler(new TableRowClickHandler() {
			@Override
			public void handleRowClick(HashMap<String, String> data, int col) {
				try {
					currentId = Integer.parseInt(data.get("id"));
					showPatientName.setText(data.get("lastname") + ", "
							+ data.get("firstname") + " ( " + "DOB: "
							+ data.get("dob") + " )");
				} catch (Exception ex) {
					GWT.log("Exception", ex);
				} finally {
					// Show the form
					flexTable.setVisible(true);
				}
			}
		});
		wTriagePendingList.setWidth("100%");

		flexTable = new FlexTable();
		verticalPanel.add(flexTable);
		flexTable.setWidth("100%");
		flexTable.setVisible(false);

		int pos = 0;

		showPatientName = new Label();
		showPatientName.setStyleName("freemed-PatientSummaryHeading");
		flexTable.setWidget(pos, 1, showPatientName);

		final Label findExistingPatient = new Label(_("Find existing patient") + " : ");
		flexTable.setWidget(++pos, 0, findExistingPatient);
		wExistingPatient = new PatientWidget();
		wExistingPatient.addChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				Integer patientSelected = event.getValue();
				if (patientSelected != null && patientSelected != 0) {
					showPatientName.setText("");
					migrateToPatient(currentId, patientSelected);
					loadVitalsScreen(patientSelected);
				}
			}
		});
		flexTable.setWidget(pos, 1, wExistingPatient);

		flexTable.setWidget(++pos, 0, new Label("or"));
		CustomButton wMigrateToPatient = new CustomButton(_("Create New Patient"),
				AppConstants.ICON_ADD_PERSON);
		wMigrateToPatient.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPatientName.setText("");
				createPatient(currentId);
			}
		});
		flexTable.setWidget(pos, 1, wMigrateToPatient);

		// Register on the event bus
		CurrentState.getEventBus().addHandler(SystemEvent.TYPE, this);

		// Last thing is to initialize, otherwise we're going to get some
		// NullPointerException errors
		loadData();
	}

	protected void loadVitalsScreen(Integer patientId) {
		PatientScreen patientScreen = new PatientScreen();
		patientScreen.setPatient(patientId);
		final PatientScreen psI = (PatientScreen) Util.spawnTab(_("Patient"),
				patientScreen);
		psI.setOnLoad(new Command() {
			@Override
			public void execute() {
				Util.spawnTabPatient(_("Vitals"), new VitalsEntry(), psI);
				clearForm();
				loadData();
			}
		});
	}

	protected void createPatient(Integer clinicRegistrationId) {
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			String[] params = { JsonUtil.jsonify(clinicRegistrationId) };
			RequestBuilder builder = new RequestBuilder(
					RequestBuilder.POST,
					URL
							.encode(Util
									.getJsonRequest(
											"org.freemedsoftware.module.ClinicRegistration.createPatient",
											params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(Request request, Throwable ex) {
						Util.showInfoMsg("TriageScreen",
								_("Failed to create new patient record."));
					}

					public void onResponseReceived(Request request,
							Response response) {
						if (Util.checkValidSessionResponse(response.getText())) {
							if (200 == response.getStatusCode()) {
								String raw = (String) JsonUtil.shoehornJson(
										JSONParser.parseStrict(response.getText()),
										"String");
								Integer r = null;
								try {
									r = Integer.parseInt(raw);
								} catch (Exception ex) {
									JsonUtil
											.debug("Unable to parse returned patient id");
								}
								if (r != null) {
									Util.showInfoMsg("TriageScreen",
											_("New patient record created."));
									loadVitalsScreen(r);
								} else {
									Util
											.showInfoMsg("TriageScreen",
													_("Failed to create new patient record."));
								}
							} else {
								Util.showInfoMsg("TriageScreen",
										_("Failed to create new patient record."));
							}
						}
					}
				});
			} catch (RequestException e) {
				JsonUtil.debug(e.getMessage());
				Util.showInfoMsg("TriageScreen",
						_("Failed to create new patient record."));
			}
		} else {
			JsonUtil.debug("NOT IMPLEMENTED YET");
		}
	}

	/**
	 * Called to request migrating a clinicregistration table entry to being
	 * associated with an existing patient table entry. Is completely backend
	 * functional, has no UI pieces of any import.
	 * 
	 * @param clinicRegistrationId
	 * @param patientId
	 */
	protected void migrateToPatient(Integer clinicRegistrationId,
			Integer patientId) {
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			String[] params = { JsonUtil.jsonify(clinicRegistrationId),
					JsonUtil.jsonify(patientId) };
			RequestBuilder builder = new RequestBuilder(
					RequestBuilder.POST,
					URL
							.encode(Util
									.getJsonRequest(
											"org.freemedsoftware.module.ClinicRegistration.migrateToPatient",
											params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(Request request, Throwable ex) {
					}

					public void onResponseReceived(Request request,
							Response response) {
						if (Util.checkValidSessionResponse(response.getText())) {
							if (200 == response.getStatusCode()) {
								Boolean r = (Boolean) JsonUtil.shoehornJson(
										JSONParser.parseStrict(response.getText()),
										"Boolean");
								if (r != null) {
								}
							} else {
							}

							// Force refresh after migrate is complete.
							loadData();
						}
					}
				});
			} catch (RequestException e) {
				JsonUtil.debug(e.getMessage());
			}
		} else {
			JsonUtil.debug("NOT IMPLEMENTED YET");
		}
	}

	/**
	 * Load table entries and reset form.
	 */
	@SuppressWarnings("unchecked")
	protected void loadData() {
		flexTable.setVisible(false);
		mainHorizontalPanel.setVisible(true);
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
			List<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
			results.add(new HashMap<String, String>() {
				private static final long serialVersionUID = -208742530745599186L;
				{
					// FIXME: need test values
					put("id", "1");
					put("uffdate", "2008-08-10");
					put("ufffilename", "testFile1.pdf");
				}
			});
			results.add(new HashMap<String, String>() {
				private static final long serialVersionUID = -2068691951700967021L;
				{
					// FIXME: need test values
					put("id", "2");
					put("uffdate", "2008-08-25");
					put("ufffilename", "testFile2.pdf");
				}
			});
			wTriagePendingList.loadData(results
					.toArray((HashMap<String, String>[]) new HashMap<?, ?>[0]));
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			wTriagePendingList.showloading(true);
			String[] params = {};
			RequestBuilder builder = new RequestBuilder(
					RequestBuilder.POST,
					URL
							.encode(Util
									.getJsonRequest(
											"org.freemedsoftware.module.ClinicRegistration.GetAll",
											params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(Request request, Throwable ex) {
						wTriagePendingList.showloading(false);
					}

					public void onResponseReceived(Request request,
							Response response) {
						if (Util.checkValidSessionResponse(response.getText())) {
							if (200 == response.getStatusCode()) {
								HashMap<String, String>[] r = (HashMap<String, String>[]) JsonUtil
										.shoehornJson(JSONParser.parseStrict(response
												.getText()),
												"HashMap<String,String>[]");
								if (r != null) {
									store = r;
									wTriagePendingList.loadData(r);
								}
							} else {
								wTriagePendingList.showloading(false);
							}
						}
					}
				});
			} catch (RequestException e) {
				JsonUtil.debug(e.getMessage());
				wTriagePendingList.showloading(false);
			}
		} else {
			JsonUtil.debug("NOT IMPLEMENTED YET");
			/*
			 * getDocumentsProxy().GetAll( new AsyncCallback<HashMap<String,
			 * String>[]>() { public void onSuccess(HashMap<String, String>[]
			 * res) { store = res; wTriagePendingList.loadData(res); }
			 * 
			 * public void onFailure(Throwable t) { GWT.log("Exception", t); }
			 * });
			 */
		}
	}

	/**
	 * Perform form validation.
	 * 
	 * @return Successful form validation status.
	 */
	protected boolean validateForm() {
		return true;
	}

	protected ModuleInterfaceAsync getModuleProxy() {
		ModuleInterfaceAsync p = null;
		try {
			p = (ModuleInterfaceAsync) Util
					.getProxy("org.freemedsoftware.gwt.client.Api.ModuleInterface");
		} catch (Exception e) {
			GWT.log("Exception", e);
		}
		return p;
	}

	public void clearForm() {
		flexTable.setVisible(false);
		currentId = null;
	}

	@Override
	public void closeScreen() {
		super.closeScreen();
		removeInstance(this);
	}

	@Override
	public void onSystemEvent(SystemEvent e) {
		if (e.getSourceModule() == "clinicregistration") {
			if (currentId == null || currentId == 0) {
				loadData();
			}
			Util.showInfoMsg("TriageScreen", _("Patients waiting for triage."));
		}
	}
}
