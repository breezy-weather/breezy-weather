/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.mf

// Duplicate
fun getFrenchDepartmentName(frenchDepartmentCode: String): String? {
    return getFrenchDepartments().firstOrNull { it.first == frenchDepartmentCode }?.second
}

fun getFrenchDepartmentCode(frenchDepartmentName: String): String? {
    return getFrenchDepartments().firstOrNull { it.second == frenchDepartmentName }?.first
}

fun getFrenchDepartments(): List<Pair<String, String>> {
    return listOf(
        Pair("01", "Ain"),
        Pair("02", "Aisne"),
        Pair("03", "Allier"),
        Pair("04", "Alpes de Hautes-Provence"),
        Pair("05", "Hautes-Alpes"),
        Pair("06", "Alpes-Maritimes"),
        Pair("07", "Ardèche"),
        Pair("08", "Ardennes"),
        Pair("09", "Ariège"),
        Pair("10", "Aube"),
        Pair("11", "Aude"),
        Pair("12", "Aveyron"),
        Pair("13", "Bouches-du-Rhône"),
        Pair("14", "Calvados"),
        Pair("15", "Cantal"),
        Pair("16", "Charente"),
        Pair("17", "Charente-Maritime"),
        Pair("18", "Cher"),
        Pair("19", "Corrèze"),
        Pair("21", "Côte-d'Or"),
        Pair("22", "Côtes d'Armor"),
        Pair("23", "Creuse"),
        Pair("24", "Dordogne"),
        Pair("25", "Doubs"),
        Pair("26", "Drôme"),
        Pair("27", "Eure"),
        Pair("28", "Eure-et-Loir"),
        Pair("29", "Finistère"),
        Pair("2A", "Corse-du-Sud"),
        Pair("2B", "Haute-Corse"),
        Pair("30", "Gard"),
        Pair("31", "Haute-Garonne"),
        Pair("32", "Gers"),
        Pair("33", "Gironde"),
        Pair("34", "Hérault"),
        Pair("35", "Ille-et-Vilaine"),
        Pair("36", "Indre"),
        Pair("37", "Indre-et-Loire"),
        Pair("38", "Isère"),
        Pair("39", "Jura"),
        Pair("40", "Landes"),
        Pair("41", "Loir-et-Cher"),
        Pair("42", "Loire"),
        Pair("43", "Haute-Loire"),
        Pair("44", "Loire-Atlantique"),
        Pair("45", "Loiret"),
        Pair("46", "Lot"),
        Pair("47", "Lot-et-Garonne"),
        Pair("48", "Lozère"),
        Pair("49", "Maine-et-Loire"),
        Pair("50", "Manche"),
        Pair("51", "Marne"),
        Pair("52", "Haute-Marne"),
        Pair("53", "Mayenne"),
        Pair("54", "Meurthe-et-Moselle"),
        Pair("55", "Meuse"),
        Pair("56", "Morbihan"),
        Pair("57", "Moselle"),
        Pair("58", "Nièvre"),
        Pair("59", "Nord"),
        Pair("60", "Oise"),
        Pair("61", "Orne"),
        Pair("62", "Pas-de-Calais"),
        Pair("63", "Puy-de-Dôme"),
        Pair("64", "Pyrénées-Atlantiques"),
        Pair("65", "Hautes-Pyrénées"),
        Pair("66", "Pyrénées-Orientales"),
        Pair("67", "Bas-Rhin"),
        Pair("68", "Haut-Rhin"),
        Pair("69", "Rhône"),
        Pair("70", "Haute-Saône"),
        Pair("71", "Saône-et-Loire"),
        Pair("72", "Sarthe"),
        Pair("73", "Savoie"),
        Pair("74", "Haute-Savoie"),
        Pair("75", "Paris"),
        Pair("76", "Seine-Maritime"),
        Pair("77", "Seine-et-Marne"),
        Pair("78", "Yvelines"),
        Pair("79", "Deux-Sèvres"),
        Pair("80", "Somme"),
        Pair("81", "Tarn"),
        Pair("82", "Tarn-et-Garonne"),
        Pair("83", "Var"),
        Pair("84", "Vaucluse"),
        Pair("85", "Vendée"),
        Pair("86", "Vienne"),
        Pair("87", "Haute-Vienne"),
        Pair("88", "Vosges"),
        Pair("89", "Yonne"),
        Pair("90", "Territoire-de-Belfort"),
        Pair("91", "Essonne"),
        Pair("92", "Hauts-de-Seine"),
        Pair("93", "Seine-Saint-Denis"),
        Pair("94", "Val-de-Marne"),
        Pair("95", "Val-d'Oise"),
        Pair("99", "Andorre")
    )
}
