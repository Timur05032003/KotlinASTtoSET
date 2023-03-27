import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExecTreeTest {


    @Test
    fun testExecTree() {
        val fooBarAst = Block(
            Let(Var("x"), Const(1)),
            Let(Var("y"), Const(0)),
            If( NEq(Var("a"), Const(0)),
                Block(
                    Let(Var("y"), Plus(Const(3), Var("x"))),
                    If( Eq(Var("b"), Const(0)),
                        Let(Var("x"), Mul(Const(2), Plus(Var("a"), Var("b")))),
                    )
                )
            ),
            Minus(Var("x"), Var("y"))
        )

        val tree = toExecTree(fooBarAst)

        assertNotNull(tree)
        assertEquals(4, tree.children.size)
        assertEquals(1, tree.children[0].children.size)
        assertEquals(1, tree.children[1].children.size)
        assertEquals(2, tree.children[2].children.size)
        assertEquals(2, tree.children[3].children.size)
    }
}
