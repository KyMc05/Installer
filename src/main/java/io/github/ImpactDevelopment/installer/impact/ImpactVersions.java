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

package io.github.ImpactDevelopment.installer.impact;

import io.github.ImpactDevelopment.installer.Installer;
import io.github.ImpactDevelopment.installer.github.Github;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImpactVersions {
    private static final List<ImpactVersionReleased> VERSIONS = Stream.of(Github.getReleases("ImpactDevelopment/ImpactReleases"))
            .filter(rel -> !rel.draft && (!rel.prerelease || Installer.args.prereleases))
            .map(ImpactVersionReleased::new)
            .filter(ver -> ver.possiblySigned() || Installer.args.noGPG)
            .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

    public static List<ImpactVersionReleased> getAllVersions() {
        return VERSIONS;
    }
}
