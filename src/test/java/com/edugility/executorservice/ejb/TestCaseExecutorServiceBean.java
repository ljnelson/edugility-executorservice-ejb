/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * Copyright (c) 2012-2012 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.executorservice.ejb;

import java.security.Principal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.ejb.embeddable.EJBContainer;

import javax.naming.Context;

import com.edugility.callables.JNDICallable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseExecutorServiceBean {

  private EJBContainer container;

  private Context context;

  private ExecutorService bean;

  private Callable<Integer> callable;

  private Callable<Principal> jndiCallable;
  
  private Runnable runnable;

  @Before
  public void setUp() throws Exception {
    assertNull(this.callable);
    this.callable = new Callable<Integer>() {
      @Override
      public final Integer call() {
        return Integer.valueOf(1);
      }
    };

    this.jndiCallable = new JNDICallable<Principal>("java:comp/EJBContext", javax.ejb.EJBContext.class, "getCallerPrincipal");
    
    assertNull(this.runnable);
    this.runnable = new Runnable() {
        @Override
        public final void run() {
          final String threadName = Thread.currentThread().getName();
          assertNotNull(threadName);
          assertTrue(threadName.startsWith("__ejb-thread-pool"));
        }
      };
    assertNull(this.bean);
    assertNull(this.context);
    assertNull(this.container);
    this.container = EJBContainer.createEJBContainer();
    assertNotNull(this.container);
    this.context = this.container.getContext();
    assertNotNull(this.context);
    final Object rawBean = this.context.lookup("java:global/classes/ExecutorServiceBean!java.util.concurrent.ExecutorService");
    assertTrue(rawBean instanceof ExecutorService);
    this.bean = (ExecutorService)rawBean;
  }

  @After
  public void tearDown() throws Exception {
    this.runnable = null;
    this.callable = null;
    this.bean = null;
    assertNotNull(this.container);
    assertNotNull(this.context);
    this.context.close();
    this.context = null;
    this.container.close();
    this.container = null;
  }
  
  @Test
  public void testSubmit() throws Exception {
    final Future<Integer> result = this.bean.submit(this.callable);
    checkResult(result);
  }

  @Test
  public void testSubmitJNDICallable() throws Exception {
    final Future<Principal> result = this.bean.submit(this.jndiCallable);
    assertNotNull(result);
    final Principal userPrincipal = result.get();
    assertNotNull(userPrincipal);
    assertEquals("ANONYMOUS", userPrincipal.getName());
  }

  @Test
  public void testExecute() throws Exception {
    this.bean.execute(this.runnable);
  }

  private static final void checkResult(final Future<Integer> result) throws Exception {
    assertNotNull(result);
    assertEquals(Integer.valueOf(1), result.get());
  }

}