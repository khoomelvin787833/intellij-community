/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.execution.configurations;

import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for run configurations which can be managed by a user and displayed in the UI.
 *
 * @see com.intellij.execution.RunManager
 * @see RunConfigurationBase
 *
 * If debugger is provided by plugin, RunConfiguration should also implement RunConfigurationWithSuppressedDefaultDebugAction
 * Otherwise (in case of disabled plugin) debug action may be enabled in UI but with no reaction
 * @see RunConfigurationWithSuppressedDefaultDebugAction
 */
public interface RunConfiguration extends RunProfile, JDOMExternalizable, Cloneable {
  /**
   * Returns the type of the run configuration.
   *
   * @return the configuration type.
   */
  @NotNull
  ConfigurationType getType();

  /**
   * Returns the factory that has created the run configuration.
   *
   * @return the factory instance.
   */
  ConfigurationFactory getFactory();

  /**
   * Sets the name of the configuration.
   *
   * @param name the new name of the configuration.
   */
  void setName(String name);

  /**
   * Returns the UI control for editing the run configuration settings.
   *
   * @return the settings editor component.
   */
  SettingsEditor<? extends RunConfiguration> getConfigurationEditor();

  /**
   * Returns the project in which the run configuration exists.
   *
   * @return the project instance.
   */
  Project getProject();

  /**
   * Creates a block of settings for a specific {@link ProgramRunner}. Can return null if the configuration has no settings specific
   * to a program runner.
   *
   * @param provider source of assorted information about the configuration being edited.
   * @return the per-runner settings.
   */
  @Nullable
  ConfigurationPerRunnerSettings createRunnerSettings(ConfigurationInfoProvider provider);

  /**
   * Creates a UI control for editing the settings for a specific {@link ProgramRunner}. Can return null if the configuration has no
   * settings specific to a program runner.
   *
   * @param runner the runner the settings for which need to be edited.
   * @return the editor for the per-runner settings.
   */
  @Nullable
  SettingsEditor<ConfigurationPerRunnerSettings> getRunnerSettingsEditor(ProgramRunner runner);

  /**
   * Clones the run configuration.
   *
   * @return a clone of this run configuration.
   */
  RunConfiguration clone();

  /**
   * Returns the unique identifier of the run configuration. The identifier does not need to be persisted between the sessions.
   *
   * @return the unique ID of the configuration.
   */
  int getUniqueID();
}
