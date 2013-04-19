package by.stub.repackaged.org.apache.commons.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import by.stub.repackaged.org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * General IO stream manipulation utilities.
 * <p/>
 * This class provides static utility methods for input/output operations.
 * <ul>
 * <li>closeQuietly - these methods close a stream ignoring nulls and exceptions
 * <li>toXxx/read - these methods read data from a stream
 * <li>write - these methods write data to a stream
 * <li>copy - these methods copy all the data from one stream to another
 * <li>contentEquals - these methods compare the content of two streams
 * </ul>
 * <p/>
 * The byte-to-char methods and char-to-byte methods involve a conversion step.
 * Two methods are provided in each case, one that uses the platform default
 * encoding and the other which allows you to specify an encoding. You are
 * encouraged to always specify an encoding because relying on the platform
 * default can lead to unexpected results, for example when moving from
 * development to production.
 * <p/>
 * All the methods in this class that read a stream are buffered internally.
 * This means that there is no cause to use a <code>BufferedInputStream</code>
 * or <code>BufferedReader</code>. The default buffer size of 4K has been shown
 * to be efficient in tests.
 * <p/>
 * Wherever possible, the methods in this class do <em>not</em> flush or close
 * the stream. This is to avoid making non-portable assumptions about the
 * streams' origin and further use. Thus the caller is still responsible for
 * closing streams after use.
 * <p/>
 * Origin of code: Excalibur.
 *
 * @version $Id: IOUtils.java 1326636 2012-04-16 14:54:53Z ggregory $
 */
public final class IOUtils {

   private IOUtils() {

   }

   private static final int EOF = -1;

   /**
    * The default buffer size ({@value}) to use for
    * {@link #copyLarge(InputStream, OutputStream)}
    */
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

   public static byte[] toByteArray(InputStream input) throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      copy(input, output);
      return output.toByteArray();
   }

   /**
    * Copy bytes from an <code>InputStream</code> to an
    * <code>OutputStream</code>.
    * <p/>
    * This method buffers the input internally, so there is no need to use a
    * <code>BufferedInputStream</code>.
    * <p/>
    * Large streams (over 2GB) will return a bytes copied value of
    * <code>-1</code> after the copy has completed since the correct
    * number of bytes cannot be returned as an int. For large streams
    * use the <code>copyLarge(InputStream, OutputStream)</code> method.
    *
    * @param input  the <code>InputStream</code> to read from
    * @param output the <code>OutputStream</code> to write to
    * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
    * @throws NullPointerException if the input or output is null
    * @throws IOException          if an I/O error occurs
    * @since 1.1
    */
   public static int copy(InputStream input, OutputStream output) throws IOException {
      long count = copyLarge(input, output);
      if (count > Integer.MAX_VALUE) {
         return -1;
      }
      return (int) count;
   }

   /**
    * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
    * <code>OutputStream</code>.
    * <p/>
    * This method buffers the input internally, so there is no need to use a
    * <code>BufferedInputStream</code>.
    * <p/>
    * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
    *
    * @param input  the <code>InputStream</code> to read from
    * @param output the <code>OutputStream</code> to write to
    * @return the number of bytes copied
    * @throws NullPointerException if the input or output is null
    * @throws IOException          if an I/O error occurs
    * @since 1.3
    */
   public static long copyLarge(InputStream input, OutputStream output)
      throws IOException {
      return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
   }

   /**
    * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
    * <code>OutputStream</code>.
    * <p/>
    * This method uses the provided buffer, so there is no need to use a
    * <code>BufferedInputStream</code>.
    * <p/>
    *
    * @param input  the <code>InputStream</code> to read from
    * @param output the <code>OutputStream</code> to write to
    * @param buffer the buffer to use for the copy
    * @return the number of bytes copied
    * @throws NullPointerException if the input or output is null
    * @throws IOException          if an I/O error occurs
    * @since 2.2
    */
   public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
      throws IOException {
      long count = 0;
      int n = 0;
      while (EOF != (n = input.read(buffer))) {
         output.write(buffer, 0, n);
         count += n;
      }
      return count;
   }
}
