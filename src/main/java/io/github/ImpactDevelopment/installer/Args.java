/*
 * This file is part of Impact Installer.
 *
 * Copyright (C) 2019  ImpactDevelopment and contributors
 *
 * See the CONTRIBUTORS.md file for a list of copyright holders
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package io.github.ImpactDevelopment.installer;

import com.beust.jcommander.Parameter;
import io.github.ImpactDevelopment.installer.impact.ImpactVersion;
import io.github.ImpactDevelopment.installer.impact.ImpactVersionDisk;
import io.github.ImpactDevelopment.installer.impact.ImpactVersionReleased;
import io.github.ImpactDevelopment.installer.impact.ImpactVersions;
import io.github.ImpactDevelopment.installer.setting.InstallationConfig;
import io.github.ImpactDevelopment.installer.setting.settings.*;
import io.github.ImpactDevelopment.installer.target.InstallationModeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Args {

    @Parameter(names = {"--no-gpg", "--disable-gpg"}, description = "Disable checking the release signature for testing purposes")
    public boolean noGPG = false;

    @Parameter(names = {"-i", "--impact-version"}, description = "The fully qualified Impact version (e.g. 4.6-1.12.2)")
    public String impactVersion;

    @Parameter(names = {"-f", "--json-file", "--file"}, description = "A json file to install from. Overrides impactVersion.")
    public String file;

    @Parameter(names = {"-m", "--mode"}, description = "The mode of installation to execute")
    public String mode;

    @Parameter(names = {"--no-gui", "--disable-gui"}, description = "Disable the GUI and execute the specifcied mode")
    public boolean noGUI = false;

    @Parameter(names = {"--pre", "--include-pre", "--prerelease", "--prereleases", "--include-prereleases"}, description = "Include releases marked as prerelease on GitHub")
    public boolean prereleases = false;

    @Parameter(names = {"--all"}, description = "Run on all Impact releases")
    public boolean all = false;

    @Parameter(names = {"--mc-dir", "--minecraft-dir", "--minecraft-directory", "--mc-path"}, description = "Path to the Minecraft directory")
    public String mcPath;

    @Parameter(names = {"--optifine", "--of"}, description = "OptiFine, in the format like 1.12.2_HD_U_E2")
    public String optifine;

    @Parameter(names = {"--no-ga", "--no-analytics", "--dnt", "--no-tracky"}, description = "Disable Google Analytics")
    public boolean noAnalytics = false;

    @Parameter(names = {"-h", "-?", "--help"}, description = "Display this help and exit", help = true, order = 0)
    public boolean showUsage = false;

    @Parameter(names = {"--version"}, description = "Output version information and exit\n", help = true, order = 1)
    public boolean showVersion = false;

    public void apply(InstallationConfig config) {
        if (mcPath != null) {
            Path path = Paths.get(mcPath);
            if (!Files.isDirectory(path)) {
                throw new IllegalStateException(path + " is not a directory");
            }
            config.setSettingValue(MinecraftDirectorySetting.INSTANCE, path);
        }
        if (mode != null) {
            config.setSettingValue(InstallationModeSetting.INSTANCE, InstallationModeOptions.valueOf(mode.toUpperCase()));
        }
        if (all) {
            for (ImpactVersionReleased version : ImpactVersions.getAllVersions()) {
                setImpactVersion(config, true, version);
                try {
                    System.out.println(config.execute());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (impactVersion != null) {
            setImpactVersion(config, true,
                    ImpactVersions.getAllVersions().stream()
                            .filter(version -> version.getCombinedVersion().equals(impactVersion))
                            .findAny()
                            .orElseThrow(() -> new IllegalArgumentException("No impact version matches " + impactVersion))
            );
        }
        if (file != null) {
            setImpactVersion(config, false, new ImpactVersionDisk(Paths.get(file)));
        }
        if (optifine != null) {
            if (!config.setSettingValue(OptiFineSetting.INSTANCE, optifine)) {
                throw new IllegalArgumentException(optifine + " is not found");
            }
        }
    }

    private void setImpactVersion(InstallationConfig config, boolean checkMcVersionValidityAgainstReleases, ImpactVersion version) {
        config.setSettingValue(MinecraftVersionSetting.INSTANCE, version.mcVersion);
        if (checkMcVersionValidityAgainstReleases && !ImpactVersionSetting.INSTANCE.validSetting(config, version)) {
            throw new IllegalStateException(impactVersion + " is not a valid selection in the current configuration. Perhaps try a different mode or version");
        }
        config.setSettingValue(ImpactVersionSetting.INSTANCE, version);
    }
}
