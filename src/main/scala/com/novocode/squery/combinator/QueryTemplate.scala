package com.novocode.squery.combinator

import com.novocode.squery.session.TypeMapper
import com.novocode.squery.combinator.sql.{QueryBuilder, SQLBuilder}

final class QueryTemplate[P, E](query: Query[ColumnBase[E]]) {
  def apply(param: P) = new AppliedQueryTemplate(built, param, query.value)
  lazy val built = QueryBuilder.buildSelect(query, NamingContext())
}

final class AppliedQueryTemplate[P, +E <: ColumnBase[_]] private[squery] (val built: SQLBuilder.Result, val param: P, val value: E)

final class Parameters[P, C <: ColumnBase[P]](p: C) {
  def flatMap[F](f: C => Query[ColumnBase[F]]): QueryTemplate[P, F] = new QueryTemplate[P, F](f(p))
  def map[F](f: C => ColumnBase[F]): QueryTemplate[P, F] = new QueryTemplate[P, F](Query(f(p)))
  def filter(f: C => Boolean): Parameters[P, C] =
    if(!f(p)) throw new SQueryException("Match failed when unpacking Parameters")
    else this
}

object Parameters {
  def apply[P1](implicit tm1: TypeMapper[P1]) = new Parameters[P1, Column[P1]](new ParameterColumn(-1, tm1))

  def apply[P1,P2](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2]
  ) = new Parameters[(P1,P2), Projection2[P1,P2]](new Projection2(
      new ParameterColumn(0, tm1),
      new ParameterColumn(1, tm2)
  ))

  def apply[P1,P2,P3](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2],
      tm3: TypeMapper[P3]
  ) = new Parameters[(P1,P2,P3), Projection3[P1,P2,P3]](new Projection3(
    new ParameterColumn(0, tm1),
    new ParameterColumn(1, tm2),
    new ParameterColumn(2, tm3)
  ))

  def apply[P1,P2,P3,P4](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2],
      tm3: TypeMapper[P3],
      tm4: TypeMapper[P4]
  ) = new Parameters[(P1,P2,P3,P4), Projection4[P1,P2,P3,P4]](new Projection4(
    new ParameterColumn(0, tm1),
    new ParameterColumn(1, tm2),
    new ParameterColumn(2, tm3),
    new ParameterColumn(3, tm4)
  ))

  def apply[P1,P2,P3,P4,P5](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2],
      tm3: TypeMapper[P3],
      tm4: TypeMapper[P4],
      tm5: TypeMapper[P5]
  ) = new Parameters[(P1,P2,P3,P4,P5), Projection5[P1,P2,P3,P4,P5]](new Projection5(
    new ParameterColumn(0, tm1),
    new ParameterColumn(1, tm2),
    new ParameterColumn(2, tm3),
    new ParameterColumn(3, tm4),
    new ParameterColumn(4, tm5)
  ))

  def apply[P1,P2,P3,P4,P5,P6](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2],
      tm3: TypeMapper[P3],
      tm4: TypeMapper[P4],
      tm5: TypeMapper[P5],
      tm6: TypeMapper[P6]
  ) = new Parameters[(P1,P2,P3,P4,P5,P6), Projection6[P1,P2,P3,P4,P5,P6]](new Projection6(
    new ParameterColumn(0, tm1),
    new ParameterColumn(1, tm2),
    new ParameterColumn(2, tm3),
    new ParameterColumn(3, tm4),
    new ParameterColumn(4, tm5),
    new ParameterColumn(5, tm6)
  ))

  def apply[P1,P2,P3,P4,P5,P6,P7](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2],
      tm3: TypeMapper[P3],
      tm4: TypeMapper[P4],
      tm5: TypeMapper[P5],
      tm6: TypeMapper[P6],
      tm7: TypeMapper[P7]
  ) = new Parameters[(P1,P2,P3,P4,P5,P6,P7), Projection7[P1,P2,P3,P4,P5,P6,P7]](new Projection7(
    new ParameterColumn(0, tm1),
    new ParameterColumn(1, tm2),
    new ParameterColumn(2, tm3),
    new ParameterColumn(3, tm4),
    new ParameterColumn(4, tm5),
    new ParameterColumn(5, tm6),
    new ParameterColumn(6, tm7)
  ))

  def apply[P1,P2,P3,P4,P5,P6,P7,P8](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2],
      tm3: TypeMapper[P3],
      tm4: TypeMapper[P4],
      tm5: TypeMapper[P5],
      tm6: TypeMapper[P6],
      tm7: TypeMapper[P7],
      tm8: TypeMapper[P8]
  ) = new Parameters[(P1,P2,P3,P4,P5,P6,P7,P8), Projection8[P1,P2,P3,P4,P5,P6,P7,P8]](new Projection8(
    new ParameterColumn(0, tm1),
    new ParameterColumn(1, tm2),
    new ParameterColumn(2, tm3),
    new ParameterColumn(3, tm4),
    new ParameterColumn(4, tm5),
    new ParameterColumn(5, tm6),
    new ParameterColumn(6, tm7),
    new ParameterColumn(7, tm8)
  ))

  def apply[P1,P2,P3,P4,P5,P6,P7,P8,P9](implicit
      tm1: TypeMapper[P1],
      tm2: TypeMapper[P2],
      tm3: TypeMapper[P3],
      tm4: TypeMapper[P4],
      tm5: TypeMapper[P5],
      tm6: TypeMapper[P6],
      tm7: TypeMapper[P7],
      tm8: TypeMapper[P8],
      tm9: TypeMapper[P9]
  ) = new Parameters[(P1,P2,P3,P4,P5,P6,P7,P8,P9), Projection9[P1,P2,P3,P4,P5,P6,P7,P8,P9]](new Projection9(
    new ParameterColumn(0, tm1),
    new ParameterColumn(1, tm2),
    new ParameterColumn(2, tm3),
    new ParameterColumn(3, tm4),
    new ParameterColumn(4, tm5),
    new ParameterColumn(5, tm6),
    new ParameterColumn(6, tm7),
    new ParameterColumn(7, tm8),
    new ParameterColumn(8, tm9)
  ))
}