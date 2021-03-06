<!--{* Smarty *}-->
<!--{*
 // $Id$
 //
 // Authors:
 //      Jeff Buchbinder <jeff@freemedsoftware.org>
 //
 // FreeMED Electronic Medical Record and Practice Management System
 // Copyright (C) 1999-2012 FreeMED Software Foundation
 //
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*}-->
<html>
<head>
	<title><!--{t}-->FreeMED Installation Wizard<!--{/t}--></title>
<!--{*
	<script type="text/javascript">
		djConfig = { isDebug: true };
	</script>
*}-->
	<script type="text/javascript" src="<!--{$htdocs}-->/dojo/dojo.js"></script>
	<script type="text/javascript">
		dojo.require("dojo.io.*");
		dojo.require("dojo.widget.Wizard");
		dojo.require("dojo.widget.Tooltip");
		dojo.require("dojo.widget.Select");
		dojo.require("dojo.widget.Button");
		dojo.require("dojo.widget.ProgressBar");
		dojo.require("dojo.widget.TimePicker");
	</script>

	<style type="text/css">

		/* Special display stuff */
		body {
			background-image: url(<!--{$htdocs}-->/images/stipedbg.png);
			margin: 2em;
			}
		h1 {
			font-variant: small-caps;
			}
		.description {
			font-style: italic;
			padding-bottom: 2em;
			}

		/* Hide minutes for time selection and "any" container */
		.minutes, .minutesHeading { display: none; }

		.dojoDialog {
			background-image: url(<!--{$htdocs}-->/images/stipedbg.png);
			border : 1px solid #999999;
			padding : 4px;
			-moz-border-radius : 5px;
		}
	</style>
</head>
<body>

<img src="<!--{$htdocs}-->/images/FreeMEDLogoTransparent.png" border="0" />

<br/>

<script language="javascript">

	function verifyDatabasePane ( ) {
		var okay = true;
		var messages = '';

		if ( ! document.getElementById('name').value ) {
			okay = false;
			messages += "<!--{t|escape:'javascript'}-->Database name has not been set.<!--{/t}-->\n";
		}
		if ( ! document.getElementById('username').value ) {
			okay = false;
			messages += "<!--{t|escape:'javascript'}-->Username has not been set.<!--{/t}-->\n";
		}
		if ( document.getElementById('password').value != document.getElementById('password_confirm').value ) {
			okay = false;
			messages += "<!--{t|escape:'javascript'}-->Password do not match.<!--{/t}-->\n";
		}

		// If something is wrong already, don't run check.
		if ( okay ) {
			// Functional check; needs to run synchronous, otherwise we
			// won't be able to wait for the status of 'okay' variable.
			dojo.io.bind({
				method : 'POST',
				sync : true,
				url: '<!--{$base_uri}-->/relay.php/json/org.freemedsoftware.public.Installation.CheckDbCredentials',
				content: {
					param0: document.getElementById('host').value,
					param1: document.getElementById('name').value,
					param2: document.getElementById('username').value,
					param3: document.getElementById('password').value
				},
				error: function(type, data, evt) {
					alert("<!--{t|escape:'javascript'}-->Please try again, data error.<!--{/t}-->");
				},
				load: function(type, data, evt) {
					if (data != true) {
						okay = false;
						messages += "<!--{t|escape:'javascript'}-->Credentials are not valid.<!--{/t}-->\n";
					} else {
						okay = true;
					}
				},
				mimetype: "text/json"
			});
		}

		if (!okay) { return messages; }
	} // end function verifyDatabasePane

	function verifyAdministrationPane ( ) {
		var okay = true;
		var messages = '';

		if ( ! document.getElementById('name').value ) {
			okay = false;
			messages += "<!--{t|escape:'javascript'}-->Database name has not been set.<!--{/t}-->\n";
		}
		if ( ! document.getElementById('adminuser').value ) {
			okay = false;
			messages += "<!--{t|escape:'javascript'}-->Username has not been set.<!--{/t}-->\n";
		}
		if ( ! document.getElementById('adminpass').value ) {
			okay = false;
			messages += "<!--{t|escape:'javascript'}-->Password has not been set.<!--{/t}-->\n";
		}
		if ( document.getElementById('adminpass').value != document.getElementById('adminpass_confirm').value ) {
			okay = false;
			messages += "<!--{t|escape:'javascript'}-->Password do not match.<!--{/t}-->\n";
		}
		if (!okay) { return messages; }
	} // end function verifyAdministrationPane

	function checkpwconfirm ( pw, pwc, out ) {
		var pw1 = document.getElementById(pw).value;
		var pw2 = document.getElementById(pwc).value;
		if ( pw1 == pw2 ) {
			document.getElementById(out).innerHTML = '';
			document.getElementById(out).style.display = 'none';
		} else {
			document.getElementById(out).innerHTML = "<!--{t|escape:'javascript'}-->Passwords do not match.<!--{/t}-->\n";
			document.getElementById(out).style.display = 'block';
		}
	} // end function checkpwconfirm

	function setProgressBar ( progress, text ) {
		var p = dojo.widget.byId('ProgressBar');
		var t = document.getElementById('ProgressBarText');
		t.innerHTML = text;
		p.setProgressValue( progress );
	} // end function setProgressBar

	function done ( ) {
		var failedToInstall = false;

		// Show the actual dialog box
		dojo.widget.byId('DoneDialog').show();

		setProgressBar( 10, "<!--{t|escape:'javascript'}-->Creating configuration file<!--{/t}-->" );

		// Start initial
		dojo.io.bind({
			method : 'POST',
			sync : true,
			url: '<!--{$base_uri}-->/relay.php/json/org.freemedsoftware.public.Installation.CreateSettings',
			content: {
				param0: {
					engine: dojo.widget.byId('engine').getValue(),
					host: document.getElementById('host').value,
					username: document.getElementById('username').value,
					password: document.getElementById('password').value,
					name: document.getElementById('name').value,
					installation: document.getElementById('installation').value,
					language: document.getElementById('language').options[document.getElementById('language').selectedIndex].value
				}
			},
			error: function(type, data, evt) {
				alert("<!--{t|escape:'javascript'}-->Please try again, data error.<!--{/t}-->");
				dojo.widget.byId('DoneDialog').hide();
				failedToInstall = true;
				return false;
			},
			load: function(type, data, evt) {
				if (!data) {
					alert("<!--{t|escape:'javascript'}-->Settings creation failed.<!--{/t}-->");
					dojo.widget.byId('DoneDialog').hide();
					return false;
				}
			},
			mimetype: "text/json"
		});

		if (failedToInstall) { return false; }
		setTimeout( null, 3000 );
		setProgressBar( 30, "<!--{t|escape:'javascript'}-->Initializing database<!--{/t}-->" );
		dojo.io.bind({
			method : 'POST',
			sync : true,
			url: '<!--{$base_uri}-->/relay.php/json/org.freemedsoftware.public.Installation.CreateDatabase',
			content: {
				param0 : document.getElementById('adminuser').value,
				param1 : document.getElementById('adminpass').value
			},
			error: function(type, data, evt) {
				alert("<!--{t|escape:'javascript'}-->Please try again, data error.<!--{/t}-->");
				dojo.widget.byId('DoneDialog').hide();
				return false;
			},
			load: function(type, data, evt) {
				if (!data) {
					alert("<!--{t|escape:'javascript'}-->Failed to create the database.<!--{/t}-->");
					dojo.widget.byId('DoneDialog').hide();
					return false;
				}
			},
			mimetype: "text/json"
		});

		if (failedToInstall) { return false; }
		setTimeout( null, 3000 );
		setProgressBar( 95, "<!--{t|escape:'javascript'}-->Making FreeMED usable<!--{/t}-->" );
		dojo.io.bind({
			method : 'POST',
			sync : true,
			url: '<!--{$base_uri}-->/relay.php/json/org.freemedsoftware.public.Installation.SetHealthyStatus',
			content: { },
			error: function(type, data, evt) {
				alert("<!--{t|escape:'javascript'}-->Please try again, data error.<!--{/t}-->");
				dojo.widget.byId('DoneDialog').hide();
				return false;
			},
			load: function(type, data, evt) {
				if (!data) {
					alert("<!--{t|escape:'javascript'}-->Failed to create the database.<!--{/t}-->");
					dojo.widget.byId('DoneDialog').hide();
					return false;
				}
			},
			mimetype: "text/json"
		});

		if (failedToInstall) { return false; }

		// Wait so we can see what happens ...
		setProgressBar( 100, "<!--{t|escape:'javascript'}-->Installation completed.<!--{/t}-->" );
		setTimeout( null, 3000 );
		setProgressBar( 100, "<a class=\"button\" href=\"<!--{$base_uri}-->/index.php\"><!--{t|escape:'javascript'}-->Click here to start using FreeMED<!--{/t}--></a>");
		//dojo.widget.byId('DoneDialog').hide();
		return true;
	} // end function done

</script>

<div id="installWizard" dojoType="WizardContainer" style="width: 95%; height: 80%;" nextButtonLabel="<!--{t|escape:'javascript'}-->Next<!--{/t}--> &gt;&gt;" previousButtonLabel="&lt;&lt; <!--{t|escape:'javascript'}-->Previous<!--{/t}-->" cancelButtonLabel="<!--{t|escape:'javascript'}-->Cancel<!--{/t}-->" cancelFunction="cancel" hideDisabledButtons="true">
	<div dojoType="WizardPane" label="Welcome to FreeMED" passFunction="initialCheck">
		<h1><!--{t}-->Welcome to FreeMED!<!--{/t}--></h1>

		<p>
		<!--{t escape="no"}-->Thank you for choosing <b>FreeMED</b> as your electronic medical record / practice management system. <b>FreeMED</b> is an opensource program, and is located on the web at <a href="http://www.freemedsoftware.org/" >http://www.freemedsoftware.org/</a>.<!--{/t}-->
		</p>

		<p>
		<!--{t escape="no"}-->This wizard will help you set up your initial <b>FreeMED</b> install by asking you questions. Please make sure that your <b>FreeMED</b> installation is writeable by your webserver user.<!--{/t}-->
		<!--{t escape="no" 1=$webroot 2=$webuser}-->( We think that your webserver root is "<b>%1</b>" and your webserver user is "<b>%2</b>". )<!--{/t}-->
		</p>

		<p>
		<!--{if not $mysqlenabled}-->
		<span style="color: #ff0000;">
		<!--{t}-->We have detected that your PHP installation either does not have support for MySQL or does not have that support enabled. Please reconfigure your PHP installation, then reload this wizard to continue.<!--{/t}-->
		</span>
		<pre>
		<script language="javascript">
			function initialCheck ( ) {
				return "<!--{t|escape:'javascript'}-->Please install or enable MySQL support in your PHP installation, then reload this wizard to continue.<!--{/t}-->";
			} // end function initialCheck
		</script>
		<!--{else}-->
		<!--{if $configwrite}-->
		<!--{t escape="no"}-->We have detected that your <b>lib/settings.php</b> file is writable, and that the installation may continue.<!--{/t}-->
		<script language="javascript">
			// Config file exists, leave null stub to keep from funny errors.
			function initialCheck ( ) { }
		</script>
		<!--{else}-->
		<span style="color: #ff0000;">
		<!--{t escape="no"}-->We have detected that your <b>lib/settings.php</b> file is NOT writable, and that the installation cannot continue. Please change the permissions on this file by issuing the following commands as root:<!--{/t}-->
		</span>
		<pre>
	touch <!--{$webroot}-->/lib/settings.php
	chown <!--{$webuser}--> <!--{$webroot}-->/lib/settings.php
	chmod +w <!--{$webroot}-->/lib/settings.php
		</pre>
		<script language="javascript">
			function initialCheck ( ) {
				return "<!--{t|escape:'javascript'}-->Please follow the instructions on this pane and reload the wizard to proceed.<!--{/t}-->";
			} // end function initialCheck
		</script>
		<!--{/if}--> <!--{* configwrite *}-->
		<!--{/if}--> <!--{* not mysqlenabled *}-->
		</p>
	</div>

	<div dojoType="WizardPane" label="Database Configuration" passFunction="verifyDatabasePane" canGoBack="false">
		<h1><!--{t}-->Database Configuration<!--{/t}--></h1>

		<div class="description">
		<!--{t}-->Please configure these options to reflect your SQL server configuration.<!--{/t}-->
		</div>

		<table border="0">
			<tr>
				<td align="right"><!--{t}-->Database Engine<!--{/t}--></td>
				<td align="left">
					<select dojoType="Select" id="engine" name="engine" widgetId="engine">
						<option value="mysql" selected>mysql</option>
					</select>
				</td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Host<!--{/t}--></td>
				<td align="left"><input type="input" id="host" name="host" value="localhost" /></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Username<!--{/t}--></td>
				<td align="left"><input type="input" id="username" name="username" value="root" /></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Password<!--{/t}--></td>
				<td align="left"><input type="password" id="password" name="password" onKeyUp="checkpwconfirm('password', 'password_confirm', 'pwconfirm');" /></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Password (confirm)<!--{/t}--></td>
				<td align="left"><input type="password" id="password_confirm" name="password_confirm" onKeyUp="checkpwconfirm('password', 'password_confirm', 'pwconfirm');" /></td>
				<td><span id="pwconfirm" style="border: 1px solid #000000; padding: 2px; color: #ff0000; display: none;"></span></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Database Name<!--{/t}--></td>
				<td align="left"><input type="input" id="name" name="name"  value="freemed" /></td>
			</tr>
			<tr>
				<td align="right"><!--{t}--><!--{/t}--></td>
				<td align="left"></td>
			</tr>
		</table>

		<!-- Tooltips for database pane -->

		<span dojoType="tooltip" connectId="engine" toggle="explode" toggleDuration="100"><!--{t escape="no"}-->Select the database engine which is to be used by <b>FreeMED</b>. <b>mysql</b> is the default.<!--{/t}--></span>
		<span dojoType="tooltip" connectId="host" toggle="explode" toggleDuration="100"><!--{t escape="no"}-->Hostname for the SQL server. This is usually "<b>localhost</b>" for most installs.<!--{/t}--></span>
		<span dojoType="tooltip" connectId="username" toggle="explode" toggleDuration="100"><!--{t escape="no"}-->MySQL username<!--{/t}--></span>
		<span dojoType="tooltip" connectId="password" toggle="explode" toggleDuration="100"><!--{t escape="no"}-->MySQL password<!--{/t}--></span>
		<span dojoType="tooltip" connectId="password_confirm" toggle="explode" toggleDuration="100"><!--{t}-->Duplicate of the MySQL password field, to ensure that you meant to type that password.<!--{/t}--></span>

		<script language="javascript">
		dojo.addOnLoad( function () {
				// Set defaults
			dojo.widget.byId('engine').setLabel('mysql');
			dojo.widget.byId('engine').setValue('mysql');
		} );
		</script>
	</div>

	<div dojoType="WizardPane" label="Administration Configuration" passFunction="verifyAdministrationPane" canGoBack="false">
		<h1><!--{t}-->Administration Configuration<!--{/t}--></h1>

		<div>
		<!--{t}-->Please configure the administrative account for this system. We recommend that you use the name "admin", but please select a relatively complex password, as this account has full access to all system data and functions.<!--{/t}-->	
		</div>

		<table border="0">
			<tr>
				<td align="right"><!--{t}-->Administrative Account<!--{/t}--></td>
				<td align="left"><input type="input" id="adminuser" name="adminuser" size="50" maxlength="50" value="admin" /></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Administrative Password<!--{/t}--></td>
				<td align="left"><input type="password" id="adminpass" name="adminpass" onKeyUp="checkpwconfirm('adminpass', 'adminpass_confirm', 'adminpwconfirm');" /></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Administrative Password (confirm)<!--{/t}--></td>
				<td align="left"><input type="password" id="adminpass_confirm" name="adminpass_confirm" onKeyUp="checkpwconfirm('adminpass', 'adminpass_confirm', 'adminpwconfirm');" /></td>
				<td><span id="adminpwconfirm" style="border: 1px solid #000000; padding: 2px; color: #ff0000; display: none;"></span></td>
			</tr>
		</table>
	</div>

	<div dojoType="WizardPane" label="System Configuration" doneFunction="done" canGoBack="false">
		<h1><!--{t}-->System Configuration<!--{/t}--></h1>

		<div>
		<!--{t}-->Please choose the configuration options which best fit your system needs.<!--{/t}-->	
		</div>

		<table border="0">
			<tr>
				<td align="right"><!--{t}-->Installation Name<!--{/t}--></td>
				<td align="left"><input type="input" id="installation" name="installation" size="50" maxlength="50" value="<!--{t|escape:'javascript'}-->Installation Name<!--{/t}-->"/></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Default Language<!--{/t}--></td>
				<td align="left"><select id="language" name="language">
					<option value="en_US">English (United States)</option>
					<option value="fr_FR">Fran&#184;ais (France)</option>
					<option value="de_DE">Deutsch (Germany)</option>
				</select></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Scheduler Start Time<!--{/t}--></td>
				<td align="left"><div id="starttime" name="starttime" dojoType="TimePicker"></div></td>
			</tr>
			<tr>
				<td align="right"><!--{t}-->Scheduler End Time<!--{/t}--></td>
				<td align="left"><div id="endtime" name="endtime" dojoType="TimePicker"></div></td>
			</tr>
		</table>
	</div>

</div>

<!--{* Create finishing progress bar etc *}-->
<div dojoType="dialog" id="DoneDialog" toggle="fade" style="display: none;">
	<div align="center">
	<div dojoType="ProgressBar" width="440" height="20"
		hasText="false" isVertical="false" maxProgressValue="100"
		id="ProgressBar"></div>
	</div>
	<div align="center" id="ProgressBarText" style="font-weight: bold;"><!--{t}-->Installing ...<!--{/t}--></div>
</div>

</body>
</html>
