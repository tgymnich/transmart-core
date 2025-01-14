ruleset {

	description '''
				A Sample Groovy RuleSet containing all CodeNarc Rules, grouped by category.
				You can use this as a template for your own custom RuleSet.
				Just delete the rules that you don't want to include.
				'''

	// rulesets/basic.xml
	AssertWithinFinallyBlock
	AssignmentInConditional
	BigDecimalInstantiation
	BitwiseOperatorInConditional
	BooleanGetBoolean
	BrokenNullCheck
	BrokenOddnessCheck
	ClassForName
	ComparisonOfTwoConstants
	ComparisonWithSelf
	ConstantAssertExpression
	ConstantIfExpression
	ConstantTernaryExpression
	DeadCode
	DoubleNegative
	DuplicateCaseStatement
	DuplicateMapKey
	DuplicateSetValue
	EmptyCatchBlock
	EmptyElseBlock
	EmptyFinallyBlock
	EmptyForStatement
	EmptyIfStatement
	EmptyInstanceInitializer
	EmptyMethod
	EmptyStaticInitializer
	EmptySwitchStatement
	EmptySynchronizedStatement
	EmptyTryBlock
	EmptyWhileStatement
	EqualsAndHashCode
	EqualsOverloaded
	ExplicitGarbageCollection
	ForLoopShouldBeWhileLoop
	HardCodedWindowsFileSeparator
	HardCodedWindowsRootDirectory
	IntegerGetInteger
	RandomDoubleCoercedToZero
	RemoveAllOnSelf
	ReturnFromFinallyBlock
	ThrowExceptionFromFinallyBlock

	// rulesets/braces.xml
	ElseBlockBraces
	ForStatementBraces
	IfStatementBraces
	WhileStatementBraces

	// rulesets/concurrency.xml
	BusyWait
	DoubleCheckedLocking
	InconsistentPropertyLocking
	InconsistentPropertySynchronization
	NestedSynchronization
	StaticCalendarField
	StaticConnection
	StaticDateFormatField
	StaticMatcherField
	StaticSimpleDateFormatField
	SynchronizedMethod
	SynchronizedOnBoxedPrimitive
	SynchronizedOnGetClass
	SynchronizedOnReentrantLock
	SynchronizedOnString
	SynchronizedOnThis
	SynchronizedReadObjectMethod
	SystemRunFinalizersOnExit
	ThreadGroup
	ThreadLocalNotStaticFinal
	ThreadYield
	UseOfNotifyMethod
	VolatileArrayField
	VolatileLongOrDoubleField
	WaitOutsideOfWhileLoop

	// rulesets/convention.xml
	ConfusingTernary
	CouldBeElvis
	HashtableIsObsolete
	// InvertedIfElse
	LongLiteralWithLowerCaseL
	ParameterReassignment
	TernaryCouldBeElvis
	VectorIsObsolete

	// rulesets/design.xml
	AbstractClassWithPublicConstructor
	AbstractClassWithoutAbstractMethod
	// BooleanMethodReturnsNull
	// BuilderMethodWithSideEffects
	CloneableWithoutClone
	// CloseWithoutCloseable
	CompareToWithoutComparable
	ConstantsOnlyInterface
	EmptyMethodInAbstractClass
	FinalClassWithProtectedMember
	// ImplementationAsType
	PrivateFieldCouldBeFinal
	PublicInstanceField
	ReturnsNullInsteadOfEmptyArray
	ReturnsNullInsteadOfEmptyCollection
//	SimpleDateFormatMissingLocale
	StatelessSingleton

	// rulesets/dry.xml
	// DuplicateListLiteral
	DuplicateMapLiteral
//	DuplicateNumberLiteral
//	DuplicateStringLiteral

	// rulesets/exceptions.xml
	CatchArrayIndexOutOfBoundsException
	CatchError
	CatchException
	CatchIllegalMonitorStateException
	CatchIndexOutOfBoundsException
	CatchNullPointerException
	CatchRuntimeException
	// CatchThrowable
	ConfusingClassNamedException
	ExceptionExtendsError
	MissingNewInThrowStatement
	ReturnNullFromCatchBlock
	SwallowThreadDeath
	ThrowError
	ThrowException
	ThrowNullPointerException
	ThrowRuntimeException
	ThrowThrowable

	// rulesets/formatting.xml
	BracesForClass
	// BracesForForLoop
	BracesForIfElse
	BracesForMethod
	BracesForTryCatchFinally
	ClassJavadoc
	LineLength

	// rulesets/generic.xml
	IllegalClassReference
	IllegalPackageReference
	IllegalRegex
	RequiredRegex
	RequiredString
	StatelessClass

	// rulesets/grails.xml
//	GrailsDomainHasEquals
//	GrailsDomainHasToString
//	GrailsPublicControllerMethod
//	GrailsServletContextReference
//	GrailsSessionReference
//	GrailsStatelessService

	// rulesets/groovyism.xml
	AssignCollectionSort
	AssignCollectionUnique
	// ClosureAsLastMethodParameter
	CollectAllIsDeprecated
	ConfusingMultipleReturns
	ExplicitArrayListInstantiation
	ExplicitCallToAndMethod
	ExplicitCallToCompareToMethod
	ExplicitCallToDivMethod
	ExplicitCallToEqualsMethod
	ExplicitCallToGetAtMethod
	ExplicitCallToLeftShiftMethod
	ExplicitCallToMinusMethod
	ExplicitCallToModMethod
	ExplicitCallToMultiplyMethod
	ExplicitCallToOrMethod
	ExplicitCallToPlusMethod
	ExplicitCallToPowerMethod
	ExplicitCallToRightShiftMethod
	ExplicitCallToXorMethod
	ExplicitHashMapInstantiation
	ExplicitHashSetInstantiation
	ExplicitLinkedHashMapInstantiation
	ExplicitLinkedListInstantiation
	ExplicitStackInstantiation
	ExplicitTreeSetInstantiation
	GStringAsMapKey
	GetterMethodCouldBeProperty
	GroovyLangImmutable
	UseCollectMany
	UseCollectNested

	// rulesets/imports.xml
	DuplicateImport
	ImportFromSamePackage
	ImportFromSunPackages
	// MisorderedStaticImports we prefer static after
	UnnecessaryGroovyImport
	// UnusedImport

	// rulesets/jdbc.xml
	DirectConnectionManagement
	// JdbcConnectionReference
	// JdbcResultSetReference
	// JdbcStatementReference

	// rulesets/junit.xml
	ChainedTest
	CoupledTestCase
	JUnitAssertAlwaysFails
	JUnitAssertAlwaysSucceeds
	JUnitFailWithoutMessage
//	JUnitPublicNonTestMethod
	JUnitSetUpCallsSuper
	JUnitStyleAssertions
	JUnitTearDownCallsSuper
	JUnitTestMethodWithoutAssert
	JUnitUnnecessarySetUp
	JUnitUnnecessaryTearDown
	SpockIgnoreRestUsed
	UnnecessaryFail
	UseAssertEqualsInsteadOfAssertTrue
	UseAssertFalseInsteadOfNegation
	UseAssertNullInsteadOfAssertEquals
	UseAssertSameInsteadOfAssertTrue
	UseAssertTrueInsteadOfAssertEquals
	UseAssertTrueInsteadOfNegation

	// rulesets/logging.xml
	LoggerForDifferentClass
	LoggerWithWrongModifiers
	LoggingSwallowsStacktrace
	MultipleLoggers
	PrintStackTrace
	// Println
	SystemErrPrint
	SystemOutPrint

	// rulesets/naming.xml
	AbstractClassName
	ClassName
	ConfusingMethodName
//	FactoryMethodName
	FieldName
	InterfaceName
//	MethodName
	ObjectOverrideMisspelledMethodName
	PackageName
	ParameterName
	PropertyName
	VariableName

	// rulesets/security.xml
	FileCreateTempFile
	InsecureRandom
	// JavaIoPackageAccess
	NonFinalPublicField
	NonFinalSubclassOfSensitiveInterface
	ObjectFinalize
	PublicFinalizeMethod
	// SystemExit
	UnsafeArrayDeclaration

	// rulesets/serialization.xml
	SerialPersistentFields
	SerialVersionUID
	SerializableClassMustDefineSerialVersionUID

	// rulesets/size.xml
//	AbcComplexity
	ClassSize
//	CrapMetric
	CyclomaticComplexity
//	MethodCount
	MethodSize
	NestedBlockDepth

	// rulesets/unnecessary.xml
	AddEmptyString
	ConsecutiveLiteralAppends
	ConsecutiveStringConcatenation
	UnnecessaryBigDecimalInstantiation
	UnnecessaryBigIntegerInstantiation
	UnnecessaryBooleanExpression
	UnnecessaryBooleanInstantiation
	UnnecessaryCallForLastElement
	UnnecessaryCallToSubstring
	UnnecessaryCatchBlock
	// UnnecessaryCollectCall
	UnnecessaryCollectionCall
	UnnecessaryConstructor
	UnnecessaryDefInFieldDeclaration
	UnnecessaryDefInMethodDeclaration
	UnnecessaryDefInVariableDeclaration
	UnnecessaryDotClass
	UnnecessaryDoubleInstantiation
	UnnecessaryElseStatement
	UnnecessaryFinalOnPrivateMethod
	UnnecessaryFloatInstantiation
//	UnnecessaryGString
	UnnecessaryGetter
	UnnecessaryIfStatement
	UnnecessaryInstanceOfCheck
	UnnecessaryInstantiationToGetClass
	UnnecessaryIntegerInstantiation
	UnnecessaryLongInstantiation
	UnnecessaryModOne
	UnnecessaryNullCheck
	UnnecessaryNullCheckBeforeInstanceOf
//	UnnecessaryObjectReferences
	UnnecessaryOverridingMethod
	UnnecessaryPackageReference
	UnnecessaryParenthesesForMethodCallWithClosure
	UnnecessaryPublicModifier
	UnnecessaryReturnKeyword
	UnnecessarySelfAssignment
	UnnecessarySemicolon
	UnnecessaryStringInstantiation
	UnnecessarySubstring
	UnnecessaryTernaryExpression
	UnnecessaryTransientModifier

	// rulesets/unused.xml
	UnusedArray
	UnusedMethodParameter
	UnusedObject
	UnusedPrivateField
	UnusedPrivateMethod
	UnusedPrivateMethodParameter
	UnusedVariable


}
