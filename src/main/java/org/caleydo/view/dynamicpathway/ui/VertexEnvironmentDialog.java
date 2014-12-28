package org.caleydo.view.dynamicpathway.ui;

import org.eclipse.swt.layout.GridLayout;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.gui.util.AHelpButtonDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * SWT Dialog -> opened (e.g. created) if edit button in controll bar is pressed
 * 
 * @author Christiane Schwarzl
 *
 */
public class VertexEnvironmentDialog extends AHelpButtonDialog {

	public static final String TITLE = "Choose Vertex Environment";
	public static final String LABEL = "Choose Vertex Environment";
	public static final String ENV_TYPE_LABEL = "Choose Environment Type:";
	public static final String ENV_TYPE_LABEL2 = "Show pathways ";

	private Integer vertexEnv;
	private ChangeVertexEnvironmentEvent changeVertexEnvEvent;

	public VertexEnvironmentDialog(Shell parentShell) {
		super(parentShell);
		this.vertexEnv = new Integer(-1);
	}

	@Override
	protected void helpPressed() {
		// TODO Auto-generated method stub
		System.out.println("TODO");
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(TITLE);

		TrayDialog trayDialog = (TrayDialog) newShell.getData();
		trayDialog.setHelpAvailable(true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite chooseEnvTypeParentComposite = new Composite(parent, 0);
		chooseEnvTypeParentComposite.setLayout(new GridLayout(1, true));

		final Label envTypeLabel = new Label(chooseEnvTypeParentComposite, SWT.NONE);
		envTypeLabel.setText(ENV_TYPE_LABEL2);
		envTypeLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		Composite chooseEnvTypeChildComposite = new Composite(chooseEnvTypeParentComposite, 0);
		chooseEnvTypeChildComposite.setLayout(new GridLayout(2, true));

		Composite chooseEnvSizeComposite = new Composite(parent, 0);
		chooseEnvSizeComposite.setLayout(new GridLayout(1, false));

		final Label envSizeLabel = new Label(chooseEnvSizeComposite, SWT.NONE);
		envSizeLabel.setText(LABEL);
		envSizeLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		final Text envSizeText = new Text(chooseEnvSizeComposite, SWT.BORDER);
		envSizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		envSizeText.setEnabled(false);

		envSizeText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				if(envSizeText.getText().length() < 1)
					return;
				int newEnvValue = Integer.parseInt(envSizeText.getText());
				System.out.println("newEnv: " + newEnvValue);
				if (newEnvValue > 0) {
					if (vertexEnv != newEnvValue && envSizeText.getEnabled() == true) {
						vertexEnv = newEnvValue;
					} else if(envSizeText.getEnabled() == false) {
						vertexEnv = -1;
					}

				} else {
					throw new NumberFormatException("Only positive values > 0 allowed");
				}
			}
		});
		envSizeText.setText("4");
		envSizeText.setToolTipText("Only positive values > 0 allowed");

		Button envTypeFullPathwayRadio = new Button(chooseEnvTypeChildComposite, SWT.RADIO);
		envTypeFullPathwayRadio.setText("fully");
		envTypeFullPathwayRadio.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		// stdDevClippingButton.addListener(SWT.Selection, listener);
		envTypeFullPathwayRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("PARTLY !!!");
				setEnOrDisabled(envSizeText, false);

			}
		});

		Button envTypePartlyPathwayRadio = new Button(chooseEnvTypeChildComposite, SWT.RADIO);
		envTypePartlyPathwayRadio.setText("partly");
		envTypePartlyPathwayRadio.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		envTypePartlyPathwayRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnOrDisabled(envSizeText, true);
			}
		});

		return parent;
	}


	private void setEnOrDisabled(final Text envSizeText, boolean enable) {
		System.out.println("Enable: " + enable);
		envSizeText.setEnabled(enable);
	}

	public Integer getVertexEnv() {
		return vertexEnv;
	}

}
