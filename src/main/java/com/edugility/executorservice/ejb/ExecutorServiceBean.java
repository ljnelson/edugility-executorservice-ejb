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

import java.util.Collections;
import java.util.List;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Stateless;

/**
 * An {@link AbstractExecutorService} implemented as a {@linkplain
 * Singleton singleton session bean} whose asynchronous behavior is
 * implemented in terms of the EJB 3.0 {@code @}{@link Asynchronous}
 * annotation.
 *
 * <h2>Implementation Notes</h2>
 *
 * <p><strong>No shutdown or termination capability.</strong> Because
 * fundamentally instances of this class are singleton session beans,
 * they cannot be {@linkplain #shutdown() shut down} or {@linkplain
 * #shutdownNow() terminated}.  Invocations of either the {@link
 * #shutdown()} or the {@link #shutdownNow()} method will either do
 * nothing or will throw a {@link SecurityException}.  Clients should
 * not invoke them.</p>
 *
 * <p><strong>No testing for shutdown or termination status.</strong>
 * Because fundamentally instances of this class are singleton session
 * beans, they are never {@linkplain #isShutdown() shut down} or
 * {@linkplain #isTerminated() terminated}.  Invocations of the {@link
 * #isShutdown()}, {@link #isTerminated()} and the {@link
 * #awaitTermination(long, TimeUnit)} method always return {@code
 * false}, or may throw {@link SecurityException}s.</p>
 *
 * <p><strong>EJB programming restrictions apply to {@linkplain
 * #submit(Callable) submitted <tt>Callable</tt> instances}</strong>.
 * Because fundamentally instances of this class are singleton session
 * beans, {@link Callable} instances that are {@linkplain
 * #submit(Callable) submitted} to an {@link ExecutorServiceBean} must
 * abide by the programming restrictions placed by the EJB
 * specification on code running within an enterprise Java bean.
 * These include but are not limited to restrictions on filesystem I/O
 * and {@link Thread} creation.  For a full list of these
 * restrictions, please consult section 21.2.2 of the EJB
 * specification.</p>
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 */
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local({Executor.class, ExecutorService.class, AsynchronousExecutor.class})
@Singleton
public class ExecutorServiceBean extends AbstractExecutorService implements ExecutorService, AsynchronousExecutor {

  @Resource
  private SessionContext sessionContext;

  public ExecutorServiceBean() {
    super();
  }

  @Override
  public void execute(final Runnable runnable) {
    if (runnable != null) {
      if (this.sessionContext != null) {
        final AsynchronousExecutor self = this.sessionContext.getBusinessObject(AsynchronousExecutor.class);
        assert self != null;
        self.executeAsynchronously(runnable);
      } else {
        runnable.run();
      }
    }
  }

  @Asynchronous
  @Override
  public void executeAsynchronously(final Runnable runnable) {
    if (runnable != null) {
      runnable.run();
    }
  }

  @Override
  public boolean awaitTermination(final long timeout, final TimeUnit unit) {
    return false;
  }

  @Override
  public boolean isTerminated() {
    return false;
  }

  @Override
  public boolean isShutdown() {
    return false;
  }

  @Override
  @PreDestroy
  public void shutdown() {

  }

  @Override
  public List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

}