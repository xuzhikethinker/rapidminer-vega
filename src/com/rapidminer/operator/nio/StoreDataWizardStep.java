package com.rapidminer.operator.nio;

import java.util.logging.Level;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.wizards.AbstractWizard;
import com.rapidminer.gui.tools.dialogs.wizards.AbstractWizard.WizardStepDirection;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.RepositoryLocationSelectionWizardStep;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.nio.model.DataResultSet;
import com.rapidminer.operator.nio.model.WizardState;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.tools.LogService;

/**
 * 
 * @author Simon Fischer
 *
 */
public final class StoreDataWizardStep extends RepositoryLocationSelectionWizardStep {
	
	private WizardState state;
	
	public StoreDataWizardStep(AbstractWizard parent, WizardState state, String preselectedLocation) {
		super(parent, preselectedLocation);
		this.state = state;
	}

	@Override
	protected boolean performLeavingAction(WizardStepDirection direction) {
		if (direction == WizardStepDirection.FINISH) {
			String repositoryLocationPath = getRepositoryLocation();
			if (repositoryLocationPath  == null) {
				return false;
			}
			final RepositoryLocation location;
			try {
				location = new RepositoryLocation(repositoryLocationPath);
			} catch (Exception e) {
				SwingTools.showSimpleErrorMessage("malformed_rep_location", e, repositoryLocationPath);
				return false;
			}					
			new ProgressThread("importing_data", true) {
				@Override
				public void run() {
					DataResultSet resultSet = null;
					try {
						if (state.getTranslator() != null) {
							state.getTranslator().close();
						}
						resultSet = state.getDataResultSetFactory().makeDataResultSet(null);
						state.getTranslator().clearErrors();
						final ExampleSet exampleSet = state.readNow(resultSet, false, getProgressListener());
						
						try {
							RepositoryManager.getInstance(null).store(exampleSet, location, null);										
						} catch (RepositoryException ex) {
							SwingTools.showSimpleErrorMessage("cannot_store_obj_at_location", ex, location);
							return;
						}
					} catch (Exception e) {
						SwingTools.showSimpleErrorMessage("cannot_store_obj_at_location", e, location);
					} finally {
						if (resultSet != null) {
							try {
								resultSet.close();
							} catch (OperatorException e) {
								LogService.getRoot().log(Level.WARNING, "Failed to close result set: "+e, e);
							}
						}	
						state.getDataResultSetFactory().close();
						getProgressListener().complete();
					}
				}
			}.start();
			return true;
		} else {
			return super.performLeavingAction(direction);
		}
	}
}