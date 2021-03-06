/*
 * Copyright 2003-2020 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ipp.bool;

import com.intellij.codeInspection.CommonQuickFixBundle;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.siyeh.ig.psiutils.BoolUtils;
import com.siyeh.ig.psiutils.CommentTracker;
import com.siyeh.ig.psiutils.ComparisonUtils;
import com.siyeh.ig.psiutils.ParenthesesUtils;
import com.siyeh.ipp.base.MutablyNamedIntention;
import com.siyeh.ipp.base.PsiElementPredicate;
import org.jetbrains.annotations.NotNull;

public class DemorgansIntention extends MutablyNamedIntention {

  @Override
  protected String getTextForElement(PsiElement element) {
    final PsiPolyadicExpression binaryExpression = (PsiPolyadicExpression)element;
    final IElementType tokenType = binaryExpression.getOperationTokenType();
    if (tokenType.equals(JavaTokenType.ANDAND)) {
      return CommonQuickFixBundle.message("fix.replace.x.with.y", "&&", "||");
    }
    else {
      return CommonQuickFixBundle.message("fix.replace.x.with.y", "||", "&&");
    }
  }

  @Override
  @NotNull
  public PsiElementPredicate getElementPredicate() {
    return new ConjunctionPredicate();
  }

  @Override
  public void processIntention(@NotNull PsiElement element) {
    final PsiPolyadicExpression polyadicExpression = (PsiPolyadicExpression)element;
    final CommentTracker tracker = new CommentTracker();
    final String newExpression = convertConjunctionExpression(polyadicExpression, tracker);
    replaceExpressionWithNegatedExpressionString(newExpression, polyadicExpression, tracker);
  }

  private static String convertConjunctionExpression(PsiPolyadicExpression polyadicExpression, CommentTracker tracker) {
    final IElementType tokenType = polyadicExpression.getOperationTokenType();
    final boolean tokenTypeAndAnd = tokenType.equals(JavaTokenType.ANDAND);
    final String flippedToken = tokenTypeAndAnd ? "||" : "&&";
    final StringBuilder result = new StringBuilder();
    for (PsiElement child : polyadicExpression.getChildren()) {
      if (child instanceof PsiJavaToken) {
        result.append(flippedToken);
      }
      else if (child instanceof PsiExpression) {
        result.append(convertLeafExpression((PsiExpression)child, tokenTypeAndAnd, tracker));
      }
      else {
        result.append(tracker.text(child));
      }
    }
    return result.toString();
  }

  private static String convertLeafExpression(PsiExpression expression, boolean tokenTypeAndAnd, CommentTracker tracker) {
    if (BoolUtils.isNegation(expression)) {
      final PsiExpression negatedExpression = BoolUtils.getNegated(expression);
      if (negatedExpression == null) {
        return "";
      }
      return safe(tracker.text(negatedExpression, tokenTypeAndAnd ? ParenthesesUtils.OR_PRECEDENCE : ParenthesesUtils.AND_PRECEDENCE)) ;
    }
    else if (ComparisonUtils.isComparison(expression)) {
      final PsiBinaryExpression binaryExpression = (PsiBinaryExpression)expression;
      final String negatedComparison = ComparisonUtils.getNegatedComparison(binaryExpression.getOperationTokenType());
      final PsiExpression lhs = binaryExpression.getLOperand();
      final PsiExpression rhs = binaryExpression.getROperand();
      if (rhs != null) {
        return safe(tracker.text(lhs)) + negatedComparison + safe(tracker.text(rhs));
      }
    }
    return '!' + safe(tracker.text(expression, ParenthesesUtils.PREFIX_PRECEDENCE));
  }

  private static String safe(String s) {
    final int length = s.length();
    if (length == 0 || s.charAt(0) != '"' || (length != 1 && s.charAt(length - 1) == '"')) {
      return s;
    }
    return s + '"';
  }
}
