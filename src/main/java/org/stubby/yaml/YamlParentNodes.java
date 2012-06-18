/*
A Java-based HTTP stub server

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.stubby.yaml;

/**
 * @author Alexander Zagniotov
 * @since 6/15/12, 8:41 AM
 */
public enum YamlParentNodes {
   REQUEST("request"),
   RESPONSE("response"),
   HEADERS("headers"),
   HTTPLIFECYCLE("httplifecycle");

   private final String description;

   private YamlParentNodes(final String description) {
      this.description = description;
   }

   public String desc() {
      return description.toLowerCase();
   }
}