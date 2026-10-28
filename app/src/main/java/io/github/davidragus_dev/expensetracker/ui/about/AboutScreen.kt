/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Expense Tracker - keep track of monthly income and expenses
 * Copyright (C) 2026 David Ruiz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.davidragus_dev.expensetracker.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.davidragus_dev.expensetracker.BuildConfig
import io.github.davidragus_dev.expensetracker.R
import io.github.davidragus_dev.expensetracker.ui.theme.ExpenseTrackerTheme

private const val LICENSE_URL = "https://www.gnu.org/licenses/gpl-3.0.html"
private const val SOURCE_CODE_URL = "https://github.com/davidragus-dev/ExpenseTracker"
private const val APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0"

/**
 * Shows the "Appropriate Legal Notices" required by the GPL for interactive programs:
 * copyright, the license, the lack of warranty and where to get the source code.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    AboutContent(versionName = BuildConfig.VERSION_NAME, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutContent(versionName: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = stringResource(R.string.about_version, versionName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(stringResource(R.string.about_copyright), style = MaterialTheme.typography.titleMedium)
            // The official notice, in English in every language
            Text(stringResource(R.string.about_license_notice))
            Text(stringResource(R.string.about_no_warranty))

            // Other languages add an unofficial translation below it
            val translationNote = stringResource(R.string.about_translation_note)
            if (translationNote.isNotEmpty()) {
                Text(
                    text = translationNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(stringResource(R.string.about_license_notice_translated))
                Text(stringResource(R.string.about_no_warranty_translated))
            }

            OutlinedButton(onClick = { uriHandler.openUri(LICENSE_URL) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_read_license))
            }
            OutlinedButton(onClick = { uriHandler.openUri(SOURCE_CODE_URL) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_source_code))
            }

            HorizontalDivider()

            Text(stringResource(R.string.about_third_party_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.about_third_party_body))
            TextButton(onClick = { uriHandler.openUri(APACHE_LICENSE_URL) }) {
                Text(stringResource(R.string.about_apache_license))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutContentPreview() {
    ExpenseTrackerTheme {
        AboutContent(versionName = "0.1.0", onBack = {})
    }
}
