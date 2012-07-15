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

package org.stubby.yaml.stubs;

import org.stubby.yaml.YamlParentNodes;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:21 AM
 */
public final class StubHttpLifecycle {

   private StubRequest request;
   private StubResponse response;
   private YamlParentNodes currentlyPopulated = YamlParentNodes.REQUEST;

   public StubHttpLifecycle(final StubRequest request, final StubResponse response) {
      this.request = request;
      this.response = response;
   }

   public StubRequest getRequest() {
      return request;
   }

   public StubResponse getResponse() {
      return response;
   }

   public final YamlParentNodes getCurrentlyPopulated() {
      return currentlyPopulated;
   }

   public final void setCurrentlyPopulated(final YamlParentNodes currentlyPopulated) {
      this.currentlyPopulated = currentlyPopulated;
   }

   public boolean isComplete() {
      return request.isConfigured() && response.isConfigured();
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubHttpLifecycle)) return false;

      final StubHttpLifecycle that = (StubHttpLifecycle) o;

      if (!request.equals(that.request)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = request.hashCode();
      result = 31 * result;
      return result;
   }

   @Override
   public String toString() {
      return "StubHttpLifecycle{" +
            "request=" + request +
            ", response=" + response +
            '}';
   }
}